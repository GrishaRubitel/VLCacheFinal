package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUsers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.LinkedList;
import java.util.Optional;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    public UsersService() {}

    public LinkedList<Users> selectAll() {
        Iterable<Users> entrySet = usersRepository.findAll();
        LinkedList<Users> entryList = new LinkedList<>();
        entrySet.forEach(entryList::add);
        return entryList;
    }

    public Optional<Users> selectByUserName(String name) {
        Iterable<Users> allUsers = usersRepository.findAll();
        for (Users user : allUsers) {
            if (user.getUsername().equals(name)) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public void insertNewUser(Users user) {
        usersRepository.save(user);
    }
}
