package com.example.CBLC_Handler.repositories;

import com.example.CBLC_Handler.entities.InputFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InputFilesRepository extends JpaRepository<InputFiles, Integer> {
}
