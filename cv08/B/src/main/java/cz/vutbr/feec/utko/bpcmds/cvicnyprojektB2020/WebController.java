package cz.vutbr.feec.utko.bpcmds.cvicnyprojektB2020;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@ServletComponentScan
public class WebController {

    @Autowired
    private ProjectResourceComponent handler;

    private List<Video> VideoList = new ArrayList<>();
    private final static String DASH_DIRECTORY = "";


    @RequestMapping(value = "/dash/{file}", method = RequestMethod.GET)
    public void streaming(@PathVariable("file") String file,
                          HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        File STREAM_FILE = null;
        STREAM_FILE = new File(DASH_DIRECTORY + file);

        request.setAttribute(ProjectResourceComponent.ATTR_FILE, STREAM_FILE);
        handler.handleRequest(request, response);
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping(value = "/getvideo/{name}")
    public void byterange(@PathVariable("name") String name, HttpServletRequest request, HttpServletResponse
            response)
            throws ServletException, IOException {

        request.setAttribute(ProjectResourceComponent.ATTR_FILE, new File(DASH_DIRECTORY + name));
        handler.handleRequest(request, response);
    }

    @GetMapping("/addVideo")
    public String addvideo(Model model) {
        model.addAttribute("video", new Video());
        return "addvideo";
    }

    @RequestMapping(value = "/videolibrary", method = {RequestMethod.POST, RequestMethod.GET})
    public String videolibrary(Model model,
                               @ModelAttribute("video") Video video,
                               @RequestParam(required = false, name = "name") String name, @RequestParam(required = false, name = "URL") String URL) {
        VideoList.add(new Video (URL,name));
        model.addAttribute("name", name);
        model.addAttribute("url", URL);
        model.addAttribute("videoList", VideoList);
        return "videolibrary";
    }

    @RequestMapping (value = "/player", method = RequestMethod.GET)
    public String player (@RequestParam int video, Model model) {
        model.addAttribute("video", VideoList.get(video));
        return "player";
    }


    }


