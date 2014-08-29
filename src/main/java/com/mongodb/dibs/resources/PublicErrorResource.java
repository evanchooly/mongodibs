package com.mongodb.dibs.resources;

import io.dropwizard.views.View;

import java.util.Random;

public class PublicErrorResource {
    private static final String[] IMAGE_401 = {"breakingbad.gif", "cameron.gif", "commando.gif", "george.gif", "jim.gif", "rashida.gif",
                                               "stewart.gif", "suspicious.gif", "tennant.gif", "tyra.gif", "uhuhuh.gif", "urkel.gif",
                                               "zapp.gif"};
    private static final String[] IMAGE_404 = {"andy.gif", "coogan.gif", "darkhelmet.gif", "jason.gif", "jesse.gif", "kramer.gif",
                                               "lloyd.gif", "modfam.gif"};
    private static final String[] IMAGE_500 = {"500.gif"};

    public View view401() {
        return new ErrorView("/error/401.ftl", getRandomImage(IMAGE_401));
    }

    public View view404() {
        return new ErrorView("/error/404.ftl", getRandomImage(IMAGE_404));
    }

    public View view500() {
        return new ErrorView("/error/500.ftl", getRandomImage(IMAGE_500));
    }

    private String getRandomImage(final String[] images) {
        return images[new Random().nextInt(images.length)];
    }

}
