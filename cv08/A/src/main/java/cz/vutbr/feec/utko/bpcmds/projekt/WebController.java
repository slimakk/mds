package cz.vutbr.feec.utko.bpcmds.projekt;

import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    // Anotace Autowired vytvoří závislost na objektu handler, který je využit v metodě byterange
    @Autowired
    private ProjectResourceComponent handler;

    //TODO Přepsat cesty pro použití v učebně
    // Deklarace objektu typu File s názvem MP4_FILE s cestou k souboru videa
    private final static String SUFFIX = "mpd";

    // Deklarace proměnné String s cestou k souborům streamu
    private final static String DASH_DIRECTORY = "";
    private final static String STREAM_DIRECTORY = "";

    @RequestMapping(value = {"/dash/{file}"}, method = RequestMethod.GET)
    public void streaming(
            @PathVariable("file") String file,
            //@PathVariable(value = "quality", required = false) String quality,
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        File STREAM_FILE = null;
        STREAM_FILE = new File(DASH_DIRECTORY + file);

        request.setAttribute(ProjectResourceComponent.ATTR_FILE, STREAM_FILE);
        handler.handleRequest(request, response);
    }

    // metoda s anotací getMapping, která při dotazu na index vrací šablonu index.html v resour
    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/videocollection")
    public ModelAndView videoCollection() throws IOException, JCodecException {
        Map<String, Object> model = new HashMap<String, Object>();
        String viewName = "videocollection";

        VideoLibrary library = new VideoLibrary();
        Collection<File> files = library.getFiles(STREAM_DIRECTORY, SUFFIX);
        List<Video> movies = library.getImages(files);

        model.put("moviesItems", movies);   //z html se volá přes klíč movieItems, předá se objekt movies
        model.put("numberOfMovies", movies.size());

        return new ModelAndView(viewName, model);
    }

    @RequestMapping(value = {"/player/{name}"}, method = RequestMethod.GET)
    public ModelAndView player(@PathVariable("name") String name) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("videoName", name);
        return new ModelAndView("player", model);
    }


    // Vytvoření metody s anotací getMapping, která při dotazu na byterange vrací soubor videa.
    // Metoda za pomocí objektu HttpServetRequest podporuje byte-range dotazy.
    // Byte range dotazování umožňuje stahovat soubor "po kouscích" - nevrací tedy celý soubor, ale pouze požadovaný počet bajtů
    // supports byte-range requests
    @GetMapping(value = "/getvideo/{name}")
    public void byterange(@PathVariable("name") String name, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute(ProjectResourceComponent.ATTR_FILE, new File(DASH_DIRECTORY + name));
        handler.handleRequest(request, response);
    }


}
