/*
 * Copyright 2017 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.sockjs;

import java.io.UnsupportedEncodingException;

public class IFrameUtils {

    public static byte[] generateIFrame(String origin) {
        StringBuilder b = new StringBuilder();
        b.append("<!DOCTYPE html>\n").append(
                "<html>\n").append(
                "<head>\n").append(
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n").append(
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").append(
                "  <script>\n").append(
                "    document.domain = document.domain;\n").append(
                "    _sockjs_onload = function(){SockJS.bootstrap_iframe();};\n").append(
                "  </script>\n").append(
                "  <script src=\"").append(origin).append("\"></script>\n").append(
                "</head>\n").append(
                "<body>\n").append(
                "  <h2>Don't panic!</h2>\n").append(
                "  <p>This is a SockJS hidden iframe. It's used for cross domain magic.</p>\n").append(
                "</body>\n").append(
                "</html>");
        try {
            return b.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return b.toString().getBytes();
        }
    }

    public static byte[] generateHtmlFile(String callback) {
        StringBuilder b = new StringBuilder();
        b.append("<!doctype html>\n").append(
                "<html><head>\n").append(
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n").append(
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").append(
                "</head><body><h2>Don't panic!</h2>\n").append(
                "  <script>\n").append(
                "    document.domain = document.domain;\n").append(
                "    var c = parent.").append(callback).append(";\n").append(
                "    c.start();\n").append(
                "    function p(d) {c.message(d);};\n").append(
                "    window.onload = function() {c.stop();};\n").append(
                "  </script>");

        try {
            return b.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return b.toString().getBytes();
        }

    }


}
