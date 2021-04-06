package com.example.CBLC_Handler.repositories;

import com.example.CBLC_Handler.entities.Input_Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Input_FilesRepository extends JpaRepository<Input_Files, Integer> {
}
