package ngoyweb.app;

import java.util.List;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.router.Route;
import ngoy.router.Router;
import ngoyweb.app.components.RouteTitlePipe;
import ngoyweb.app.components.StarterComponent;
import ngoyweb.app.components.TitleComponent;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = { "app.component.css" })
@NgModule(declarations = { RouteTitlePipe.class, TitleComponent.class, StarterComponent.class })
public class AppComponent {
	public final String title = "ngoy";

	@Inject
	public Router router;

	public List<Route> getRoutes() {
		return router.getRoutes();
	}

	public boolean isActiveRoute(Route route) {
		return router.isActive(route);
	}
}
