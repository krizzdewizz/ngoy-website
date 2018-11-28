package ngoyweb.app;

import java.util.List;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.OnInit;
import ngoy.router.Route;
import ngoy.router.Router;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = { "app.component.css" })
public class AppComponent implements OnInit {
	public final String title = "ngoy";

	@Inject
	public Router router;

	public Route activeRoute;

	public List<Route> getRoutes() {
		return router.getRoutes();
	}

	@Override
	public void ngOnInit() {
		activeRoute = getRoutes().stream()
				.filter(router::isActive)
				.findFirst()
				.orElse(null);
	}
}
