package will.dev.qrcodeApp.service;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import will.dev.qrcodeApp.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String password;
    private User.Role role;
    private Boolean actif;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           User.Role role) {//
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static UserDetailsImpl build(User user) {
        User.Role role = user.getRole();

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}

