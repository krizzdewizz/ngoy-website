package ngoyweb.app.components;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;
import ngoy.router.Route;

@Pipe("routeTitle")
public class RouteTitlePipe implements PipeTransform {

	@Override
	public Object transform(Object obj, Object... args) {
		Route route = (Route) obj;
		switch (route.getPath()) {
		case "index":
			return "Home";
		case "motivation":
			return "Motivation";
		case "get-started":
			return "Getting started";
		case "tutorial":
			return "Tutorial";
		default:
			return route.getPath();
		}
	}
}
