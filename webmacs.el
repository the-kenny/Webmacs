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

(defvar webmacs-process-name "webmacs connection")

(defun webmacs-after-change (start end length)
  (when (equal (current-buffer) (get-buffer "footest"))
    (process-send-string "*webmacs*" (format "%S" (webmacs-generate-change-query start end length)))))

(defun webmacs-publish-buffer (buffer-name)
  (interactive "b")
  (let ((buffer (get-buffer buffer-name)))
   (open-network-stream webmacs-process-name "*webmacs*" "localhost" 9881)
   (add-to-list 'after-change-functions #'webmacs-after-change)
   (process-send-string "*webmacs*" (format "%S" (webmacs-generate-buffer-data buffer)))))

;; (open-network-stream webmacs-process-name "*webmacs*" "localhost" 9881)

;; (process-send-string "*webmacs*" (format "%S" (webmacs-generate-buffer-data)))

;; (md5 (get-buffer "footest"))

;; (add-to-list 'after-change-functions #'webmacs-after-change)
;; (setq-default after-change-functions '())

(provide 'webmacs)
