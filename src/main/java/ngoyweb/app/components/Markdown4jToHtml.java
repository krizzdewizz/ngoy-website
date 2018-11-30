package ngoyweb.app.components;

import static ngoy.core.NgoyException.wrap;

import java.io.IOException;

import org.markdown4j.Markdown4jProcessor;

public class Markdown4jToHtml implements MarkdownToHtml {

	private final Markdown4jProcessor markdownProcessor = new Markdown4jProcessor();

	@Override
	public String convert(String markdown) {
		try {
			return markdownProcessor.process(markdown);
		} catch (IOException e) {
			throw wrap(e);
		}
	}

}
