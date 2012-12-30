(ns bill.init-tasks
  (:require bill.tasks.clean
            bill.tasks.deps
            bill.tasks.install
            bill.tasks.install-file
            bill.tasks.install-maven))

; This file does nothing but load other files with tasks.