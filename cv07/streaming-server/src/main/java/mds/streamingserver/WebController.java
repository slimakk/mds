package mds.streamingserver;

import mds.streamingserver.component.MyResourceHttpRequestHandler;
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

@Controller
public class WebController {

    //----------------------------Cviceni 6 - stream----------------------------
    //----player.html
    //----videoMP4stream.html

    //Component pro zasílání potřebných částí souboru videa
    private MyResourceHttpRequestHandler handler;

    // Anotace Autowired vytvoří závislost na objektu handler, který je využit v metodě byterange
    @Autowired
    public WebController(MyResourceHttpRequestHandler handler) {
        this.handler = handler;
    }


    // Deklarace objektu typu File s názvem MP4_FILE s cestou k souboru videa
    private final static File MP4_FILE = new File("D:\\MDS\\files\\videos\\bbb_1080p.mp4");


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
    //----------------------------Cviceni 6 - stream----------------------------


    //----------------------------Ukol - stream----------------------------
    //----index.html


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
            // vložení vlastního atributu jako th:moje="${value}", bude ve výsledku zapsán jako moje="David"
            // pokud model obsahuje atribut value s hodnotou David.


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







    //----------------------------Cviceni 7 - adaptivni stream----------------------------
    //---dashPlayer.html


    // Deklarace proměnné String s cestou k souborům streamu
    private final static String HLS_PATH = "D:\\MDS\\files\\streams\\HLS\\";
    private final static String DASH_PATH = "D:\\MDS\\files\\streams\\MPEG-DASH\\";


    // Vytvoření metody s anotací RequestMapping, která naslouchá na třech adresách a odpovídá pouze na metodu GET
    // Metoda naslouchá na třech adresách /dash a /hls
    // Podle předaného názvu souboru vrací daný soubor
    // Při obdržení dotazu je vytovřen String, kde je předán vzor, který odpovídá adresám v anotaci
    // Podle vzoru je dále ve switchi přiřazena adresa vedoucí k souborům streamu
    // Stream HLS potřebuje dvě proměnné, protože jednotlivé streamy jsou uloženy ve složkách odpovídajícím profilu
    // HLS lze otestovat přes VLC: http://localhost:8080/hls/playlist.m3u8
    @RequestMapping(value = {"/dash/{file}", "/hls/{file}", "/hls/{quality}/{file}"}, method = RequestMethod.GET)
    public void adaptive_streaming(
            @PathVariable("file") String file,
            @PathVariable(value = "quality", required = false) String quality,
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        File STREAM_FILE = null;

        //Získání patternu z adresy. Vybere takovou, která nejlépe odpovídá (kombinace value u RequestMapping a PathVariable v proměnných)
        String handle = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        //-----Samostatná práce, napsat switch-----
        switch (handle) {
            case "/dash/{file}":
                STREAM_FILE = new File(DASH_PATH + file);
                break;
            case "/hls/{file}":
                STREAM_FILE = new File(HLS_PATH + file);
                break;
            case "/hls/{quality}/{file}":
                if (!StringUtils.isEmpty(quality)) {
                    STREAM_FILE = new File(HLS_PATH + quality + "\\" + file);
                }
                break;
            default:
                break;
        }
        //-----Samostatná práce, napsat switch-----

        request.setAttribute(MyResourceHttpRequestHandler.ATTR_FILE, STREAM_FILE);
        handler.handleRequest(request, response);
    }

    //----------------------------Cviceni 7 - adaptivni stream----------------------------


    //----------------------------Ukol - adaptivni stream----------------------------
    //----index.html - doplnit odkazy/formulář pro výběr zdrojů videa
    //                  lokální mp4 (file), video z url (http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4)
    //                  stream z url (https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd)
    //                  lokální streamy mpeg dash (dash/manifest.mpd) a hls stream (hls/playlist.m3u8)
    //----dashPlayer.html - upravit, přidat změnu kvality


    @RequestMapping(value = "dashPlayer", method = {RequestMethod.POST})
    public String dashPlayer(Model model, @RequestParam String url) {
        model.addAttribute("url", url);

        //Type pro vyřešení problémů při přehrávání non dash streamů s controls
        String type = "stream";
        if(url.contains(".mpd")) type = "stream";
        else if (url.contains("hls")) type = "hls";
        else type = "video";

        model.addAttribute("type", type);
        return "dashPlayer";
    }

    @GetMapping("gallery")
    public String galerie() {
        return "gallery";
    }
    //----------------------------Ukol - adaptivni stream----------------------------


}
