package mds.streamingserver;

import mds.streamingserver.component.MyResourceHttpRequestHandler;
import model.MovieLibrary;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.thymeleaf.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static mds.streamingserver.FilePath.*;

@Controller
public class WebController {

    //Component pro zasílání potřebných částí souboru videa
    private MyResourceHttpRequestHandler handler;

    // Anotace Autowired vytvoří závislost na objektu handler, který je využit v metodě byterange
    @Autowired
    public WebController(MyResourceHttpRequestHandler handler) {
        this.handler = handler;
    }


    // Deklarace objektu typu File s názvem MP4_FILE s cestou k souboru videa



    @GetMapping("/video")
    public String video() {
        return "videoMP4stream";
    }


    // Vytvoření metody s anotací getMapping, která při dotazu na byterange vrací soubor videa.
    // Metoda za pomocí objektu HttpServetRequest podporuje byte-range dotazy.
    // Byte range dotazování umožňuje stahovat soubor "po kouscích" - nevrací tedy celý soubor, ale pouze požadovaný počet bajtů
    // Lze spustit i ve VLC: http://localhost:8080/byterange
    @GetMapping("/byterange")
    public void byterange(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Vytvoření požadavku na soubor z proměnné String MP4_FILE
        request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE, MP4_FILE);

        handler.handleRequest(request, response);
    }


    // Vytvoření metody s anotací getMapping, která při dotazu na file vrací celý soubor videa.
    // Anotace GetMapping obsahuje kromě cesty také typ souboru, který poskytuje.
    // Metoda, která nepodporuje byte-range dotazování.
    // Vrací celý soubor.
    // Lze přehrát i ve VLC: http://localhost:8080/file
    @GetMapping(path = "/file", produces = "video/mp4")
    @ResponseBody
    public FileSystemResource wholeFile() {
        return new FileSystemResource(MP4_FILE);
    }


    //----------------------------Ukol - stream----------------------------

    //Index stránka s formulářem
    @GetMapping("/index")
    public String index() {
        return "index";
    }

    //Testovací URL
    //http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
    //https://ia804503.us.archive.org/15/items/kikTXNL6MvX6ZpRXM/kikTXNL6MvX6ZpRXM.mp4
    //http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4
    //http://media.developer.dolby.com/DDP/MP4_HPL40_30fps_channel_id_51.mp4


    // Metoda dostává parametry z formuláře pomocí metody POST. Označení name u každé položky input mi předává jejich hodnoty
    // do stejně pojmenovaných parametrů zde v metodě. Plus je vhodné nastavit některé výchozí hodnoty.
    // Atributy muted a autoplay fungují tak, že pokud je nechci využít, tak se v elementu nesmí vůbec objevit.
    // Zde pomůže Thymeleaf, který odstraní atributy s prázdnou hodnotou. Naopak, když je chci použít, tak si zde
    // vložím nějakou hodnotu. Takže hodnota true zde může obsahovat jakékoliv slovo, obsah je nepodstatný.
    @RequestMapping(value = "player", method = {RequestMethod.POST})
    public String player(@RequestParam String url,
                         @RequestParam(defaultValue = "false") boolean muted,
                         @RequestParam(defaultValue = "false") boolean autoplay,
                         @RequestParam(defaultValue = "1000") String width, //Šířka jako string umožňí zapsat i %, takže můžu definovat velikost v procentech
                         Model model) {

        if (!StringUtils.isEmpty(url)) {
            model.addAttribute("url", url);
            model.addAttribute("width", width);
            model.addAttribute("muted", muted ? "true" : ""); //zjisteni, zda je true, pokud ano, naplni se string, jinak je prázdný
            model.addAttribute("autoplay", autoplay); //Autoplay se chová bohužel jinak, ten bere true/false
            // muted totiž není součástí atributů Thymeleafu, takže pracuje tak, jako bylo řečeno. Pokud je hodnota prázdná,
            // tak se atribut nevloží, naopak pokud něco obsahuje, tak se vloží.
            // Kdežto autoplay je součástí Thymeleafu a funguje jako true/false.

            // Nápověda: https://attacomsian.com/blog/thymeleaf-custom-html-attributes
            // Starší thymeleaf nepoddporoval vlastní názvy atributů a bylo nutné používat th:attr, dnes již funguje
            // vložení vlastního atributu jako th:moje="${hodnota}", bude ve výsledku zapsán jako moje="David"
            // Pokud model obsahuje atribut hodnota s hodnotou David.


            //----------------Druhá možnost jak použít muted a autoplay, pomocí th:attrapend
//            model.addAttribute("muted", muted ? "true" : "");
//            model.addAttribute("autoplay", autoplay ? "true" : "");

        } else {
            model.addAttribute("error", "Nebyla zadána žádná adresa videa!");
            //Existuje další spousta možností, jak řešit prázdné URL. Například až na straně šablony, či jinak.
        }
        return "player";
    }

    //----------------------------Ukol - stream----------------------------


    @RequestMapping(value={"/dash/{file}", "/hls/{file}", "/hls/{quality}/{file}"}, method = RequestMethod.GET)
    public void adaptive_streaming(
            @PathVariable("file") String file,
            @PathVariable(value = "quality", required = false) String quality,
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        File STREAM_FILE = null;

        String handle = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        switch (handle){
            case "/dash/{file}" :
                STREAM_FILE = new File(DASH_PATH+file);
                break;
            case "/hls/{file}"  :
                STREAM_FILE = new File(HLS_PATH+file);
                break;
            case "/hls/{quality}/{file}" :
                STREAM_FILE = new File(HLS_PATH+quality+"\\"+file);
                break;
            default:
                STREAM_FILE=null;
                break;
        }

        request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE, STREAM_FILE);
        handler.handleRequest(request, response);
    }

    @RequestMapping(value = "dashPlayer", method = {RequestMethod.GET, RequestMethod.POST})
    public String dashPlayer(@RequestParam String file, Model model){
        model.addAttribute("url", file);
        return "dashPlayer";
    }
    private MovieLibrary library = null;
    @RequestMapping("gallery")
    public String gallery(Model model) throws JCodecException, IOException {

        if(library == null)
        {
            library = new MovieLibrary(IMAGES_DIRECTORY, MP4_DIRECTORY,SUFFIX,150);
        }
        model.addAttribute("library", library);
        return "gallery";
    }

    @RequestMapping(value="/video/{file}", method = RequestMethod.GET)
    public String showVideo(@PathVariable String file, Model model){
        model.addAttribute("movieName", file);
        return "showVideo";
    }
    @RequestMapping("/getvideo/{file}")
    public void getVideo(@PathVariable String file,HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Vytvoření požadavku na soubor z proměnné String MP4_FILE
        request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE, new File (MP4_DIRECTORY+file));

        handler.handleRequest(request, response);
    }

}
