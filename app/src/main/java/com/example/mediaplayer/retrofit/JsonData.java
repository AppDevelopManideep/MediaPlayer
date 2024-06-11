package com.example.mediaplayer.retrofit;

public class JsonData {

    String title,image,sourceurl;
    public JsonData(String title,String image,String sourceurl){
        this.title=title;
        this.image=image;
        this.sourceurl=sourceurl;
    }







    public String getTitle() {
        return title;
    }


    public String getUrl() {

        return sourceurl;
    }




   public String getImage(){
        return image;
   }

}
