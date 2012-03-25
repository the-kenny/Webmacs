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

(defvar webmacs-problematic-minor-modes '(auto-complete-mode)
  "List of minor modes which cause problems when used together with `webmacs-mode'")

(defun webmacs-process-sentinel (process event)
  (when (equal (process-status process) 'closed)
    ;; TODO: Make this more visible
    (error "Lost webmacs connection. Please reconnect using `webmacs-open-connection'")))

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
    (with-current-buffer buffer
     (when webmacs-mode
       (webmacs-publish-buffer (current-buffer))))))

;;; TODO: Error checking
(defun webmacs-send-data (data)
  (process-send-string webmacs-buffer-name (format "%S" data)))

(defun webmacs-narrow-p (buffer-or-name)
  (with-current-buffer (get-buffer buffer-or-name)
    (or (not (equal 1 (point-min))) (not (equal (- (point-max) (point-min)) (buffer-size))))))

(defun webmacs-publish-buffer (buffer-or-name)
  (let ((buffer (get-buffer buffer-or-name)))
    (with-current-buffer buffer
      (webmacs-send-data (webmacs-generate-buffer-data buffer))
      (when (webmacs-narrow-p buffer-or-name) ;Narrowing is active
        (webmacs-send-data (list 'narrow (buffer-name) (point-min) (point-max)))))))

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
  (with-current-buffer (or buffer (current-b))
    (save-restriction
      (ad-disable-advice 'widen 'after 'webmacs-widen)
      (ad-activate 'widen)
      (widen)
      (ad-enable-advice 'widen 'after 'webmacs-widen)
      (ad-activate 'widen)
      (list 'buffer-data (buffer-name buffer) mode-name (buffer-size) (webmacs-encode-string (buffer-string))))))

(defun webmacs-after-change (start end length)
  (if (processp (get-buffer-process webmacs-buffer-name))
      (webmacs-send-data (webmacs-generate-change-query start end length))
    (message "No webmacs connection. Please open it using `webmacs-open-connection'")))

;;; Narrowing function

;;; TODO: Store all adviced fns in a field
;;; TODO: Add `webmacs-without-narrow' macro
(defmacro webmacs-advice-narrow (f)
  `(defadvice ,f (after ,(intern (format "webmacs-%S" f)) activate)
     (when webmacs-mode
       (webmacs-send-data (list 'narrow (buffer-name) (point-min) (point-max))))))

(webmacs-advice-narrow narrow-to-region)
(webmacs-advice-narrow narrow-to-page)
(webmacs-advice-narrow narrow-to-defun)

(eval-after-load 'org
  (progn
   (webmacs-advice-narrow org-narrow-to-subtree)
   (webmacs-advice-narrow org-narrow-to-block)
   t))

(defadvice widen (after webmacs-widen activate)
  (when webmacs-mode
    (webmacs-send-data (list 'widen (buffer-name) (point-min) (point-max)))))

;;; Utility functions for problematic minor modes

(defun webmacs-find-problematic-minor-modes (&optional buffer)
  (with-current-buffer (or buffer (current-buffer))
    (remove-if (lambda (s) (not (buffer-local-value s (current-buffer)))) webmacs-problematic-minor-modes)))

;;; TODO: Add `webmacs-mode-no-warn' to enable `webmacs-mode' programmatically
;;; even when no connection is open

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
        (error "No webmacs connection. Please open one using `webmacs-open-connection'")
        (when-let (problematic (webmacs-find-problematic-minor-modes))
          (message "The following active minor modes can cause problems with webmacs: %S" problematic))
        (add-hook 'after-change-functions #'webmacs-after-change nil 'local)
        (webmacs-publish-buffer (buffer-name)))
      (webmacs-mode 0))
    (remove-hook 'after-change-functions #'webmacs-after-change 'local)))


(provide 'webmacs)

;;; webmacs.el ends here
