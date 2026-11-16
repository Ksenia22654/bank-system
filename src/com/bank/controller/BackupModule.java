package com.bank.controller;

import com.bank.model.User;
import com.bank.service.BackupService;

/** Сохранение данных на сервере и загрузка ранее сохранённых данных. */
public class BackupModule extends CommandModule {
    private final BackupService backups = new BackupService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/backups", User.ADMIN, (req, resp) -> {
            req.setAttribute("backups", backups.list());
            return View.jsp("backups");
        });
        r.post("/backups/save", User.ADMIN, (req, resp) -> {
            Flash.success(req, "Данные сохранены в файл " + backups.save());
            return View.redirect("/backups");
        });
        r.post("/backups/restore", User.ADMIN, (req, resp) -> {
            String file = req.getParameter("file");
            backups.restore(file);
            Flash.success(req, "Данные загружены из файла " + file);
            return View.redirect("/backups");
        });
        r.post("/backups/delete", User.ADMIN, (req, resp) -> {
            backups.delete(req.getParameter("file"));
            Flash.success(req, "Файл резервной копии удалён");
            return View.redirect("/backups");
        });
    }
}
