package io.imunity.furms.views.helloworld;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import io.imunity.furms.views.main.MainView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;

@Route(value = "hello", layout = MainView.class)
@PageTitle("Hello World")
@CssImport("./styles/views/helloworld/hello-world-view.css")
@RouteAlias(value = "", layout = MainView.class)
public class HelloWorldView extends HorizontalLayout {

    private Label label;

    public HelloWorldView() {
        setId("hello-world-view");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        label = new Label("Your name: " + auth.getName());
        add(label);
    }

}
