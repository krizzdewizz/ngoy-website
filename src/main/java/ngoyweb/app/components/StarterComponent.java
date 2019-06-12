package ngoyweb.app.components;

import ngoy.core.Component;
import ngoy.core.Input;

@Component(selector = "ngoy-starter", templateUrl = "starter.component.html")
public class StarterComponent {
    @Input
    public String title;

    @Input
    public String forApp;
}
