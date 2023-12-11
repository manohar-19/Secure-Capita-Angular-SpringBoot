package com.capita.securecapita.implimentation;

import com.capita.securecapita.exception.ApiException;
import com.capita.securecapita.model.Role;
import com.capita.securecapita.model.User;
import com.capita.securecapita.repository.RoleRepository;
import com.capita.securecapita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import static com.capita.securecapita.enumeration.RoleType.ROLE_USER;
import static com.capita.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.capita.securecapita.query.UserQuery.*;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {

    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    protected final BCryptPasswordEncoder encoder;
    @Override
    public User create(User user) {
        // check the email is unique
        if(getEmailCount(user.getEmail().trim().toLowerCase())>0) throw new ApiException("Email already in use. Please use different email and try again.");
        
        //save new user
        try{
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameterSource = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY,parameterSource,holder);
            user.setId(requireNonNull(holder.getKey()).longValue());

            // add role to the user
            roleRepository.addRoleToUser(user.getId(),ROLE_USER.name());

            // send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(),ACCOUNT.getType());

            // Save URL in verification table
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_QUERY, Map.of("userId",user.getId(),"url",verificationUrl));

            // send email to user with verification URL
            // emailService.sendVerificationUrl(user.getFirstname(),user.getEmail(),verificationUrl,ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);

            // return newly created user
            return user;


        }catch (EmptyResultDataAccessException e){
            throw new ApiException("No Role found by name: "+ ROLE_USER.name());
        }catch (Exception e){
            throw new ApiException("An error occurred. Please try again.");
        }


    }


    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email",email),Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return  new MapSqlParameterSource()
                .addValue("firstName",user.getFirstname())
                .addValue("lastName",user.getLastname())
                .addValue("email",user.getEmail())
                .addValue("password",encoder.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key,String type){
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("user/verify/"+type+"/"+key).toUriString();
    }
}
