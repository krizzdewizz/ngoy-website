package ngoyweb.app.components;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.dom.NgoyElement.getPosition;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.parseHtml;
import static ngoy.core.dom.XDom.removeContents;

import java.io.IOException;
import java.io.InputStream;

import jodd.jerry.Jerry;
import ngoy.core.Component;
import ngoy.core.NgoyException;
import ngoy.core.OnCompile;
import ngoy.core.Util;

@Component(selector = "markdown")
public class MarkdownComponent implements OnCompile {

	private MarkdownToHtml mdToHtml = new CommonMarkToHtml();

	@Override
	public void ngOnCompile(Jerry el, String componentClass) {
		String url = el.attr("url");
		String text;
		if (url == null) {
			text = el.text();
		} else {
			text = readResource(format("/ngoyweb/app/%s", url));
		}
		String html = mdToHtml.convert(text);
		Jerry parsed = parseHtml(html, getPosition(el).getLine());
		removeContents(el);
		appendChild(el, parsed);
	}

	private String readResource(String url) {
		try (InputStream in = getClass().getResourceAsStream(url)) {
			if (in == null) {
				throw new NgoyException("Markdown resource not found: %s", url);
			}
			return Util.copyToString(in);
		} catch (IOException e) {
			throw wrap(e);
		}
	}
}
