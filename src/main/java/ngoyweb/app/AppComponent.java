package ngoyweb.app;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.markdown.MarkdownModule;
import ngoy.router.Route;
import ngoy.router.Router;

import java.util.List;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = {"app.component.css"})
@NgModule(imports = {MarkdownModule.class})
public class AppComponent implements OnInit {
    public final String title = "ngoy";

    @Inject
    public Router router;

    public Route activeRoute;

    public List<Route> getRoutes() {
        return router.getRoutes();
    }

    @Override
    public void onInit() {
        activeRoute = getRoutes().stream()
                .filter(router::isActive)
                .findFirst()
                .orElse(null);
    }
}
