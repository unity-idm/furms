package io.imunity.furms.views.login;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Login")
public class LoginScreen extends VerticalLayout
{

    private static final String URL = "/oauth2/authorization/unity";

    public LoginScreen()
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        Anchor login = new Anchor(URL, "Login with Unity");
        layout.add(login);
        add(layout);
        setSizeFull();
    }
}