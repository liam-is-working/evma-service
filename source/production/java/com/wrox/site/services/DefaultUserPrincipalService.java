package com.wrox.site.services;

import com.wrox.site.entities.UserAuthority;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.repositories.UserAuthorityRepository;
import com.wrox.site.repositories.UserPrincipalRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

@Service
public class DefaultUserPrincipalService implements UserPrincipalService{
    private static final SecureRandom RANDOM;
    private static final int HASHING_ROUNDS = 10;

    static
    {
        try
        {
            RANDOM = SecureRandom.getInstanceStrong();
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Inject UserPrincipalRepository userRepository;
    @Inject UserAuthorityRepository authorityRepository;

    @Override
    @Transactional
    public UserPrincipal loadUserByUsername(String username)
    {
        UserPrincipal principal = userRepository.getUserPrincipalByUsername(username);
        return principal;
    }

    @Override
    @Transactional
    public void saveUser(UserPrincipal principal, String newPassword, String roleName)
    {
        if(newPassword != null && newPassword.length() > 0)
        {
            String salt = BCrypt.gensalt(HASHING_ROUNDS, RANDOM);
            principal.setHashedPassword(
                    BCrypt.hashpw(newPassword, salt).getBytes()
            );
        }

        if(roleName!=null){
            UserAuthority role = authorityRepository.getByAuthority(roleName);
            Set<UserAuthority> roles = new HashSet<>();
            roles.add(role);
            if(role!=null){
                principal.setAuthorities(roles);
            }
        }


        this.userRepository.save(principal);
    }
}
