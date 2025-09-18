package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUserRepository IUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Entrando no método loadUserByUsername...");

        UserInfo user = Optional.ofNullable(IUserRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Não foi possível encontrar o usuário com o nome de usuário:%s", username)));

        log.info("Usuário autenticado com sucesso..!!!");

        return new CustomUserDetails(user);
    }

}
