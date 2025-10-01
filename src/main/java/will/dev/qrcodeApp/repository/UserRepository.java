package will.dev.qrcodeApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import will.dev.qrcodeApp.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCodeAcces(String codeAcces);

    Optional<User> findByEmailAndCodeAcces(String email, String codeAcces);

    Optional<User> findByEmailAndPassword(String email, String password);

    List<User> findByRole(User.Role role);

    List<User> findByActif(Boolean actif);

    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoles(@Param("roles") List<User.Role> roles);

    boolean existsByEmail(String email);

    boolean existsByCodeAcces(String codeAcces);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.actif = true")
    long countActiveUsersByRole(@Param("role") User.Role role);
}

