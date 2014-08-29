package com.mongodb.dibs.resources;

import com.google.common.base.Charsets;
import io.dropwizard.views.View;

public class ErrorView extends View {

    private String image;

    public ErrorView(final String template, final String image) {
        super(template, Charsets.ISO_8859_1);
        this.image = image;
    }

    public String getImage() {
        return image;
    }

}
