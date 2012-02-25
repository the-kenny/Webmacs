(defvar webmacs-buffer-name "*webmacs*")
(defvar webmacs-process-name "webmacs connection")

(defvar webmacs-host "localhost")
(defvar webmacs-port 9881)

(defun webmacs-open-connection (host port)
  (interactive (list (read-from-minibuffer "Host: " webmacs-host)
                     (read-from-minibuffer "Port: " (format "%d" webmacs-port)
                                           nil t)))
  (open-network-stream webmacs-process-name "*webmacs*" host port))

(defun webmacs-publish-buffer (buffer-name)
  (interactive "b")
  (let ((buffer (get-buffer buffer-name)))
    ;; (add-to-list 'after-change-functions #'webmacs-after-change)
    (process-send-string webmacs-buffer-name (format "%S" (webmacs-generate-buffer-data buffer)))))

(defun webmacs-encode-string (string)
  (base64-encode-string (encode-coding-string string 'utf-8) 'no-newline))

(defun webmacs-generate-change-query (change-start change-end pre-change-length)
  (cond
   ((= start end) (list 'delete (buffer-name) start (+ start length)))
   ((= 0 length) (list 'insert (buffer-name) start end (webmacs-encode-string (buffer-substring start end))))
   (t (list 'replace (buffer-name) start end (webmacs-encode-string (buffer-substring start end))))))

(defun webmacs-generate-buffer-data (&optional buffer)
  (with-current-buffer (or buffer (current-buffer))
    (list 'buffer-data (buffer-name buffer) (buffer-size) (webmacs-encode-string (buffer-string)))))

(defun webmacs-after-change (start end length)
  (if (processp (get-buffer-process webmacs-buffer-name))
    (process-send-string webmacs-buffer-name (format "%S" (webmacs-generate-change-query start end length)))
    (message "No webmacs connection. Please open it using `webmacs-open-connection'")))

(define-minor-mode webmacs-mode
  ""
  :init-value nil
  :lighter " webmacs"
  :group 'webmacs

  (if webmacs-mode
    (progn
      (if (not (get-buffer-process webmacs-buffer-name))
        (message "No webmacs connection. Please open one using `webmacs-open-connection'")
        ;; (if (yes-or-no-p "No webmacs connection. Open one? ")
        ;;     (call-interactively #'webmacs-open-connection))
        (add-hook 'after-change-functions #'webmacs-after-change nil 'local)
        (webmacs-publish-buffer (buffer-name))))
    (remove-hook 'after-change-functions #'webmacs-after-change 'local)))

;; (open-network-stream webmacs-process-name "*webmacs*" "localhost" 9881)

;; (process-send-string "*webmacs*" (format "%S" (webmacs-generate-buffer-data)))

;; (md5 (get-buffer "footest"))

;; (add-to-list 'after-change-functions #'webmacs-after-change)
;; (setq-default after-change-functions '())

(provide 'webmacs)
