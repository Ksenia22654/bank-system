package com.bank.controller;

import com.bank.service.AnalyticsService;
import com.bank.util.Json;

/** Страница аналитики и данные диаграмм в формате JSON. */
public class AnalyticsModule extends CommandModule {
    private final AnalyticsService analytics = new AnalyticsService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/analytics", (req, resp) -> {
            req.setAttribute("summary", analytics.summary(currentUser(req)));
            return View.jsp("analytics");
        });
        r.get("/analytics/data", (req, resp) -> {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(Json.write(analytics.chartData(currentUser(req))));
            return View.none();
        });
    }
}
