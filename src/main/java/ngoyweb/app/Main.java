package ngoyweb.app;

import static ngoy.core.Provider.useValue;

import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ngoy.Ngoy;
import ngoy.router.Location;
import ngoy.router.RouterConfig;
import ngoy.router.RouterModule;
import ngoyweb.app.doc.DocComponent;
import ngoyweb.app.getstarted.GetStartedComponent;
import ngoyweb.app.home.HomeComponent;
import ngoyweb.app.motivation.MotivationComponent;

@Controller
@RequestMapping("/*")
public class Main implements InitializingBean {
	private Ngoy<AppComponent> ngoy;

	@Autowired
	private HttpServletRequest request;

	@GetMapping()
	public void home(HttpServletResponse response) throws Exception {
		// re-create while developing to have changes picked-up
		createApp();
		ngoy.render(response.getOutputStream());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createApp();
	}

	private void createApp() {
		RouterConfig routerConfig = RouterConfig //
				.baseHref("/")
				.location(useValue(Location.class, () -> request.getRequestURI()))
				.route("index", HomeComponent.class)
				.route("get-started", GetStartedComponent.class)
				.route("doc", DocComponent.class)
				.route("motivation", MotivationComponent.class)
				.build();

		ngoy = Ngoy.app(AppComponent.class)
				.modules(RouterModule.forRoot(routerConfig))
				.modules(Main.class.getPackage())
				.build();
	}

	public static void main(String[] args) {
		Main main = new Main();
		main.createApp();
		main.ngoy.renderSite(Paths.get("docs"));
	}
}