package ngoyweb.app;

import static ngoy.core.Provider.useValue;

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
import ngoyweb.app.home.HomeComponent;

@Controller
@RequestMapping("/*")
public class Main implements InitializingBean {

	// must be disabled in production!
	private static final boolean DEV = true;

	private Ngoy<App> ngoy;

	@Autowired
	private HttpServletRequest request;

	@GetMapping()
	public void home(HttpServletResponse response) throws Exception {
//		ngoy.renderSite(java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "ngoy-starter-web-router"));

		if (DEV) {
			createApp();
		}

		ngoy.render(response.getOutputStream());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createApp();
	}

	private void createApp() {
		RouterConfig routerConfig = RouterConfig //
				.baseHref("/router")
				.location(useValue(Location.class, () -> request.getRequestURI()))
				.route("index", HomeComponent.class)
				.build();

		ngoy = Ngoy.app(App.class)
				.modules(RouterModule.forRoot(routerConfig))
				.build();
	}
}