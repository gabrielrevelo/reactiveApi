package com.example.reactive.service;

import com.example.reactive.model.Book;
import com.example.reactive.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Flux<Book> getAllBook(){
        return bookRepository.findAll().log().delayElements(Duration.ofSeconds(1));
    }

    public Flux<Book> getAllBookBackPresure(int limitRequest){
        Flux<Book> bookFlux = bookRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
        return bookFlux.limitRate(limitRequest);
    }

    public Mono<Book> findById(String id){
        return bookRepository.findById(id);
    }

    public Mono<Book> postBook(Book book) {
        return bookRepository.save(book).log();
    }

    public Mono<ResponseEntity<Book>> updateBook(String id, Book book) {
        return bookRepository.findById(id)
                .flatMap(oldBook -> {
                    oldBook.setTitle(book.getTitle());
                    oldBook.setAuthor(book.getAuthor());
                    return bookRepository.save(oldBook);
                })
                .map(updatedBook -> new ResponseEntity<>(updatedBook, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.OK));
    }

    public Mono<Book> deleteUser(String id) {
        return bookRepository.findById(id)
                .flatMap(deletedBook -> bookRepository.delete(deletedBook)
                        .then(Mono.just(deletedBook)));
    }

}
