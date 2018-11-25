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
import ngoyweb.app.getstarted.GetStartedComponent;
import ngoyweb.app.home.HomeComponent;
import ngoyweb.app.motivation.MotivationComponent;
import ngoyweb.app.tutorial.TutorialComponent;
import ngoyweb.app.tutorial.editor.EditorComponent;

@Controller
@RequestMapping("/*")
public class Main implements InitializingBean {

	// must be disabled in production!
	private static final boolean DEV = true;

	private Ngoy<AppComponent> ngoy;

	@Autowired
	private HttpServletRequest request;

	@GetMapping()
	public void home(HttpServletResponse response) throws Exception {

		if (DEV) {
			createApp();
		}

		// renderSite();

		ngoy.render(response.getOutputStream());
	}

	void renderSite() {
		createApp();
		ngoy.renderSite(java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "ngoy-website"));
		createApp();
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
				.route("tutorial", TutorialComponent.class)
				.route("tutorial-editor", EditorComponent.class)
				.route("motivation", MotivationComponent.class)
				.build();

		ngoy = Ngoy.app(AppComponent.class)
				.modules(RouterModule.forRoot(routerConfig))
				.build();
	}

	public static void main(String[] args) {
		Main main = new Main();
		main.createApp();
		main.ngoy.renderSite(Paths.get("../ngoy/docs"));
	}
}