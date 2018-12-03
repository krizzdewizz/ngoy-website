package ngoyweb.app.components.markdown;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.Input;

@Component(selector = "markdown-toc", templateUrl = "markdown-toc.component.html", styleUrls = { "markdown-toc.component.css" })
public class MarkdownTocComponent {
	private static final Set<String> EXCLUDE = new HashSet<>(asList( //
			"class-attribute" //
			, "ngclass-attribute" //
			, "style-attribute" //
			, "ngstyle-attribute" //
			, "ngif" //
			, "ngswitch" //
			, "ngfor" //
	));

	@HostBinding("class.d-none")
	public final boolean dNone = true;

	@HostBinding("class.d-md-block")
	public final boolean dMdBlock = true;

	public List<List<String>> entries;

	@Input
	public void setEntries(List<List<String>> entries) {
		this.entries = entries.stream()
				.filter(e -> !EXCLUDE.contains(e.get(0)))
				.collect(toList());
	}
}
