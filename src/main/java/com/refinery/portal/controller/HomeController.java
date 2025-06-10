package com.refinery.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.refinery.portal.service.MessageBoardService;
import com.refinery.portal.service.WhatsNewService;

@Controller
public class HomeController {

    @Autowired
    private WhatsNewService whatsNewService;
    
    @Autowired
    private MessageBoardService messageBoardService;

    @GetMapping("/")
    public String home(Model model) {
        // Add data for dashboard widgets
        model.addAttribute("whatsNewItems", whatsNewService.getActiveWhatsNewForDashboard());
        model.addAttribute("activeItemCount", whatsNewService.getActiveWhatsNewCount());
        
        // Add message board data for dashboard
        model.addAttribute("messageBoardItems", messageBoardService.getTopActiveMessages(5));
        model.addAttribute("activeMessageCount", messageBoardService.countActiveMessages());
        
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return home(model);
    }
} 