package controller;

import org.springframework.stereotype.Controller;

@Controller
public class ViewController {

    public String getIndex(){
        return "menu page";
    }

}
