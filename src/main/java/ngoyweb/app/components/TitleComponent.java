package ngoyweb.app.components;

import ngoy.core.Component;
import ngoy.core.Input;

@Component(selector = "ngoy-title", template = "<h1 class=\"mt-5 ngoy-title\">{{title}}</h1>")
public class TitleComponent {
	@Input
	public String title;
}
