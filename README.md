# Sistema de Controle de Riscos no Ambiente de Trabalho

## Sobre

Este repositório contém um aplicativo de Identificação e Registro de Riscos, parte de um projeto integrador desenvolvido no curso de Sistemas de Informação.  
O objetivo é permitir que colaboradores façam notificações de riscos no ambiente de trabalho de forma simples, com fotos, localização, descrição, setor e controle inicial desses registros.

## Funcionalidades

- Login de usuário via Firebase Authentication  
- Registro de riscos com geolocalização (latitude e longitude)  
- Upload de imagem do local do risco  
- Descrição, identificação do colaborador e setor do local  
- Armazenamento de dados no Firebase Realtime Database  

## Tecnologias

- Kotlin 
- Firebase (Authentication, Realtime Database)  
- Google Maps API

## Usuário de Teste

- Email: teste@email.com  
- Senha: teste123 

## Execução do Aplicativo

- Realizar o login com o usuário fornecido
- Clicar em "Adicionar localização" e aguardar pop-up confirmando geolocalização
- Clicar em "Adicionar imagem" e selecionar uma imagem da galeria
- Inserir nome completo e setor da ocorrência
- Clicar em enviar