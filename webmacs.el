;;; webmacs.el --- webmacs is a package to publish buffers live on a website

;;; Commentary:
;;

;;; Code:

(require 'ls-lisp)                      ;For ls-lisp-format-file-size

(defvar webmacs-buffer-name "*webmacs*")
(defvar webmacs-process-name "webmacs connection")

(defvar webmacs-host "localhost")
(defvar webmacs-port 9881)

(defvar webmacs-warning-threshold 10000)

(defun webmacs-process-sentinel (process event)
  (when (equal (process-status process) 'closed)
    ;; TODO: Make this more visible
    (message "Lost webmacs connection. Please reconnect using `webmacs-open-connection'")))

(defun webmacs-open-connection (host port)
  "Open a connection to the webmacs server running on HOST at PORT.
This is needed prior running command `webmacs-mode' for webmacs to be able to
communicate with the server.
Closes other open connections if any.

Argument HOST The hostname of the machine running the webmacs server.
Argument PORT The listen port of the webmacs server."
  (interactive (list (read-from-minibuffer "Host: " webmacs-host)
                     (read-from-minibuffer "Port: " (format "%d" webmacs-port)
                                           nil t)))
  (let ((process (make-network-process :name webmacs-process-name
                                :buffer webmacs-buffer-name
                                :host host
                                :service port)))
    (set-process-sentinel process #'webmacs-process-sentinel))

  (dolist (buffer (buffer-list))
    (when (buffer-local-value webmacs-mode buffer)
      (webmacs-publish-buffer buffer))))

;;; TODO: Error checking
(defun webmacs-send-data (data)
  (process-send-string webmacs-buffer-name (format "%S" data)))

(defun webmacs-publish-buffer (buffer-or-name)
  (let ((buffer (get-buffer buffer-or-name)))
    (webmacs-send-data (webmacs-generate-buffer-data buffer))))

(defun webmacs-encode-string (string)
  (base64-encode-string (encode-coding-string string 'utf-8) 'no-newline))

;;; TODO: Disable narrowing if active
;;; OR (better): Enable narrowing on the client side
(defun webmacs-generate-change-query (change-start change-end pre-change-length)
  (cond
   ((= change-start change-end) (list 'delete (buffer-name) change-start
                                      (+ change-start pre-change-length)))

   ((= 0 pre-change-length) (list 'insert (buffer-name) change-start
                                  (webmacs-encode-string (buffer-substring change-start change-end))))

   ;; `pre-change-length' chars are removed from the buffer (starting at `change-start')
   ;; `change-start' and `change-end' specify the region of the text *after* the replacement
   (t (list 'replace (buffer-name) change-start (+ change-start pre-change-length)
            (webmacs-encode-string (buffer-substring change-start change-end))))))

(defun webmacs-generate-buffer-data (&optional buffer)
  (with-current-buffer (or buffer (current-buffer))
    (list 'buffer-data (buffer-name buffer) mode-name (buffer-size) (webmacs-encode-string (buffer-string)))))

(defun webmacs-after-change (start end length)
  (if (processp (get-buffer-process webmacs-buffer-name))
      (webmacs-send-data (webmacs-generate-change-query start end length))
    (message "No webmacs connection. Please open it using `webmacs-open-connection'")))

;;; Narrowing function

;;; TODO: Enable only in buffers with webmacs-mode enabled
(defadvice narrow-to-region (after webmacs-narrow-to-region)
  (when webmacs-mode
   (message "webmacs-narrow-to-region")))

(defadvice widen (after webmacs-widen)
  (when webmacs-mode
    (message "webmacs-widen")))

(ad-activate 'narrow-to-region)
(ad-activate 'widen)

(define-minor-mode webmacs-mode
  "Toggle webmacs publishing.
With no argument, this command toggles the mode.
Non-null prefix argument turns on the mode.
Null prefix argument turns off the mode.

When webmacs-mode is enabled in a buffer, any changes are sent to a
server and can be watched in real time using a websocket capable
browser.
This command assumes there is already an open connection opened via
command `webmacs-open-connection'."
  :init-value nil
  :lighter " webmacs"
  :group 'webmacs

  (if webmacs-mode
    (if (or (< (buffer-size) webmacs-warning-threshold)
              (yes-or-no-p (format "Buffer %s is large (%s). Continue? " (buffer-name) (ls-lisp-format-file-size (buffer-size) t))))
      (if (not (get-buffer-process webmacs-buffer-name))
        (message "No webmacs connection. Please open one using `webmacs-open-connection'")
        ;; (if (yes-or-no-p "No webmacs connection. Open one? ")
        ;;     (call-interactively #'webmacs-open-connection))
        (add-hook 'after-change-functions #'webmacs-after-change nil 'local)
        (webmacs-publish-buffer (buffer-name)))
      (webmacs-mode 0))
    (remove-hook 'after-change-functions #'webmacs-after-change 'local)))

(provide 'webmacs)

;;; webmacs.el ends here
