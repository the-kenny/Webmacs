(defun webmacs-encode-string (string)
  (base64-encode-string (encode-coding-string string 'utf-8) 'no-newline))

(defun webmacs-generate-change-query (change-start change-end pre-change-length)
  (cond
   ((= start end) (list 'delete start (+ start length)))
   ((= 0 length) (list 'insert start end (webmacs-encode-string (buffer-substring start end))))
   (t (list 'replace start end (webmacs-encode-string (buffer-substring start end))))))

(defun webmacs-generate-buffer-data (&optional buffer)
  (with-current-buffer (or buffer (current-buffer))
    (list 'buffer-data (buffer-size) (buffer-file-name) (webmacs-encode-string (buffer-string)))))

(defvar webmacs-process-name "webmacs connection")

(defun webmacs-after-change (start end length)
  (when (equal (current-buffer) (get-buffer "footest"))
   ;; (message "Start: %S, End: %S, length: %d" start end length)
    (message "%S" (webmacs-generate-change-query start end length))
    (let (process (get-process webmacs-process-name))
      (process-send-string "*webmacs*" (format "%S" (webmacs-generate-change-query start end length))))))

;; (open-network-stream webmacs-process-name "*webmacs*" "localhost" 9881)

;; (process-send-string (get-process webmacs-process-name) (format "%S" (webmacs-generate-buffer-data)))

;; (md5 (get-buffer "footest"))

;; (add-to-list 'after-change-functions #'webmacs-after-change)
;; (setq-default after-change-functions '())

(provide 'webmacs)
