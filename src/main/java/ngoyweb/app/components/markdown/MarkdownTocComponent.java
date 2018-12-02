package ngoyweb.app.components.markdown;

import java.util.List;

import ngoy.core.Component;
import ngoy.core.Input;

@Component(selector = "markdown-toc", templateUrl = "markdown-toc.component.html", styleUrls = { "markdown-toc.component.css" })
public class MarkdownTocComponent {
	@Input
	public List<List<String>> entries;
}
