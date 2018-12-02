package ngoyweb.app.components.markdown;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.dom.NgoyElement.getPosition;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.parseHtml;
import static ngoy.core.dom.XDom.removeContents;

import java.io.IOException;
import java.io.InputStream;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Node.NodeType;
import ngoy.core.Component;
import ngoy.core.NgoyException;
import ngoy.core.OnCompile;
import ngoy.core.Util;
import ngoy.core.dom.NodeVisitor;
import ngoy.core.dom.XDom;
import ngoyweb.app.components.CommonMarkToHtml;
import ngoyweb.app.components.MarkdownToHtml;

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

		boolean hasToc = el.get(0)
				.hasAttribute("toc");

		removeContents(el);
		appendChild(el, parsed);
		if (hasToc) {
			Jerry toc = createToc(parsed);
			appendChild(parsed, toc);
		}
	}

	private Jerry createToc(Jerry parsed) {
		Jerry toc = XDom.createElement("markdown-toc", 0);

		StringBuilder expr = new StringBuilder();

		XDom.accept(parsed, new NodeVisitor() {

			@Override
			public void start(Jerry el) {

				Node ell = el.get(0);
				if (ell.getNodeType() == NodeType.ELEMENT && ell.getNodeName()
						.startsWith("h")) {
					int level = Integer.parseInt(ell.getNodeName()
							.substring(1));

					if (expr.length() == 0) {
						expr.append("{");
					} else {
						expr.append(",");
					}

					expr.append("{'");
					expr.append(el.attr("id"));
					expr.append("','");
					expr.append(el.text());
					expr.append("',");
					expr.append(level);
					expr.append("}");
				}
			}

			@Override
			public void end(Jerry arg0) {
			}

		});

		expr.append("}");
		toc.attr("[entries]", expr.toString());
		return toc;
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
