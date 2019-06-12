package ngoyweb.app.doc;

import ngoy.core.Component;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@Component(selector = "doc", templateUrl = "doc.component.html", styleUrls = {"doc.component.css"})
public class DocComponent {
    public static final Set<String> TOC_EXCLUDE = new HashSet<>(asList(
            "class-attribute"
            , "ngclass-attribute"
            , "style-attribute"
            , "ngstyle-attribute"
            , "ngif"
            , "ngswitch"
            , "ngfor"
            , "interpolation"
            , "attribute-binding"
            , "hostbinding"
            , "built-in-functions"
            , "lambdas"
            , "smart-strings"
            , "field-access-to-getter"
            , "listmap-index-access"
            , "prohibited-syntax"
            , "pipes-1"
    ));

}
