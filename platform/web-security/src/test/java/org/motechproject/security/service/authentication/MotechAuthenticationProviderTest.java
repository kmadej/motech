package org.motechproject.security.service.authentication;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.security.authentication.MotechPasswordEncoder;
import org.motechproject.security.domain.MotechRole;
import org.motechproject.security.domain.MotechUser;
import org.motechproject.security.domain.MotechUserProfile;
import org.motechproject.security.domain.UserStatus;
import org.motechproject.security.repository.AllMotechRoles;
import org.motechproject.security.repository.AllMotechUsers;
import org.motechproject.security.service.AuthoritiesService;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Locale;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MotechAuthenticationProviderTest {

    @Mock
    private AllMotechUsers allMotechUsers;
    @Mock
    private MotechPasswordEncoder passwordEncoder;
    @Mock
    private AllMotechRoles allMotechRoles;

    @Mock
    private AuthoritiesService authoritiesService;

    private MotechAuthenticationProvider authenticationProvider;

    @Before
    public void setup() {
        initMocks(this);
        authenticationProvider = new MotechAuthenticationProvider(allMotechUsers, passwordEncoder, authoritiesService);
    }

    @Test
    public void shouldRetrieveUserFromDatabase() {
        MotechUser motechUser = new MotechUser("bob", "encodedPassword", "entity_1", "", asList("some_role"), "", Locale.ENGLISH);
        MotechRole motechRole = new MotechRole("some_role", asList("some_permission"), false);
        when(allMotechUsers.findByUserName("bob")).thenReturn(motechUser);
        when(allMotechRoles.findByRoleName("some_role")).thenReturn(motechRole);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("bob", "password");
        UserDetails userDetails = authenticationProvider.retrieveUser("bob", authentication);

        assertEquals("encodedPassword", userDetails.getPassword());
        assertEquals(motechUser.getUserName(), ((MotechUserProfile) authentication.getDetails()).getUserName());
        assertEquals(motechUser.getUserName(), userDetails.getUsername());
    }

    @Test(expected = AuthenticationException.class)
    public void shouldThrowExceptionIfUserDoesntExist() {
        when(allMotechUsers.findByUserName("bob")).thenReturn(null);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("bob", "password");
        authenticationProvider.retrieveUser("bob", authentication);
    }

    @Test(expected = LockedException.class)
    public void shouldThrowExceptionIfUserIsBlocked() {
        MotechUser motechUser = new MotechUser("bob", "encodedPassword", "entity_1", "", asList("some_role"), "", Locale.ENGLISH);
        motechUser.setUserStatus(UserStatus.BLOCKED);
        motechUser.setFailureLoginCounter(3);
        when(allMotechUsers.findByUserName("bob")).thenReturn(motechUser);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("bob", "password");
        authenticationProvider.retrieveUser("bob", authentication);
    }

    @Test
    public void shouldAuthenticateUser() {
        when(passwordEncoder.isPasswordValid("encodedPassword", "password")).thenReturn(true);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("bob", "password");
        UserDetails user = mock(UserDetails.class);
        when(user.getPassword()).thenReturn("encodedPassword");

        authenticationProvider.additionalAuthenticationChecks(user, authentication);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateEmptyPassword() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("bob", "");
        UserDetails user = mock(UserDetails.class);
        when(user.getPassword()).thenReturn("encodedPassword");

        authenticationProvider.additionalAuthenticationChecks(user, authentication);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateWrongPassword() {
        when(passwordEncoder.isPasswordValid("encodedPassword", "password")).thenReturn(false);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("bob", "");
        UserDetails user = mock(UserDetails.class);
        when(user.getPassword()).thenReturn("encodedPassword");

        authenticationProvider.additionalAuthenticationChecks(user, authentication);
    }
}
