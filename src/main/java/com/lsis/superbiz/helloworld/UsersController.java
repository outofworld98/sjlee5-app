package com.lsis.superbiz.helloworld;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UsersController {

    @Value("${welcome.message}")
    public String message;

    private final UserRepository userRepository;
    private final DistributionSummary userSummary;
    private final Counter actionCounter;

    public UsersController(UserRepository userRepository, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;

        userSummary = meterRegistry.summary("user.summary");
        actionCounter = meterRegistry.counter("user.actionCounter");
    }

    @GetMapping("/hello")
    public String sayHello() {
        return  this.message;
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        userRepository.save(user);
        actionCounter.increment();
        userSummary.record(userRepository.count());

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<User> read(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user != null) {
            actionCounter.increment();
            return new ResponseEntity(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> list() {
        actionCounter.increment();
        return new ResponseEntity(userRepository.findAll(), HttpStatus.OK);
    }

    @ResponseBody
    @PutMapping("{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userRepository.save(user);

        if (updatedUser != null) {
            actionCounter.increment();
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        actionCounter.increment();
        userSummary.record(userRepository.count());
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
