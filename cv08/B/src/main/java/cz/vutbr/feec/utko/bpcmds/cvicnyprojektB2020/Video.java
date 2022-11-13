package cz.vutbr.feec.utko.bpcmds.cvicnyprojektB2020;

public class Video {

    private String URL = null;
    private String name = null;

  public Video (){
  }

  public Video (String URL, String name){
      this.URL = URL;
      this.name = name;
  }
    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
