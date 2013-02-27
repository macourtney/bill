(ns bill.init-tasks
  (:require bill.tasks.clean
            bill.tasks.deps
            bill.tasks.help
            bill.tasks.install
            bill.tasks.install-file
            bill.tasks.install-maven
            bill.tasks.jar
            bill.tasks.uberjar))

; This file does nothing but load other files with tasks.