# rimfrost-framework-regel changelog

Changelog of rimfrost-framework-regel.

## 0.3.10 (2026-02-26)

### Bug Fixes

-  set responsetopic as type ([6018f](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/6018f05d629124e) Nils Elveros)  
-  added logger to kafkaproducer ([76842](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/76842e9029bb162) Nils Elveros)  

## 0.3.9 (2026-02-25)

### Bug Fixes

-  quarkus.build.skip för core pom.xml ([0394a](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/0394a2d9bf9c22f) Ulf Slunga)  

## 0.3.8 (2026-02-24)

### Bug Fixes

-  Remove explicit kundbehovsflode jaxrs dependency ([897ec](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/897ec80bf73a222) Lars Persson)  

## 0.3.7 (2026-02-24)

### Bug Fixes

-  flytta test-definitioner från framework maskinell ([dbcc7](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/dbcc717f7dbd4eb) Ulf Slunga)  

## 0.3.6 (2026-02-24)

### Bug Fixes

-  bump adapter version ([e0a6f](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/e0a6f8502d9bcc2) Nils Elveros)  

## 0.3.5 (2026-02-23)

### Bug Fixes

-  dependency management till parent pom ([dc0c8](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/dc0c80c2e0d33d3) Ulf Slunga)  

## 0.3.4 (2026-02-23)

### Bug Fixes

-  FKPOC-406 lägger till base-class för test & delat upp i multi-naven repo ([18faf](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/18faf5dac9b17ec) Ulf Slunga)  

### Other changes

**Tar bort dependency jersey-apache-connector och lägger till kundbehovsflode-artifakt till application.properties**


[fca0f](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/fca0fe0a65929f2) Ulf Slunga *2026-02-23 13:59:22*

**Tar bort RegelServiceInterface.java och ProcessRegelRequest.java**


[e5312](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/e5312590da6d700) Ulf Slunga *2026-02-23 13:19:08*

**Ny kundbehovsflöde adapter samt använder jersey apache connector**


[4f0e9](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/4f0e92421b92d8c) Ulf Slunga *2026-02-23 13:19:08*

**spotless apply**


[2e4c3](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/2e4c31acf201ece) Ulf Slunga *2026-02-23 13:19:08*


## 0.3.3 (2026-02-23)

### Bug Fixes

-  update kundbehovadapter version ([85d5d](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/85d5d41a1f59cda) Nils Elveros)  

## 0.3.2 (2026-02-23)

### Bug Fixes

-  Set utforarId on kundbehovsflode PUT request ([0d2d5](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/0d2d5393c5f75a9) Lars Persson)  

## 0.3.1 (2026-02-20)

### Bug Fixes

-  Update functionality to work with manual framework ([235f6](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/235f6456f89f36b) Lars Persson)  

## 0.3.0 (2026-02-19)

### Features

-  Updated kundbehovsadapter with new patch and put ([83a11](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/83a1193bc24bd43) Nils Elveros)  

### Bug Fixes

-  some small changes ([37e2c](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/37e2cb12db604bd) Nils Elveros)  
-  moving some stuff to framework-regel-maskinell ([9a81d](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/9a81d63633ec13c) Nils Elveros)  
-  renamed file ([7783d](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/7783d4c03f3d3b6) Nils Elveros)  

### Other changes

**Merge branch 'main' into feat/fkpoc-403-use-new-kundbehovsadapter**


[ce09e](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/ce09e581cbcdc99) Nils Elveros *2026-02-19 09:16:25*


## 0.2.3 (2026-02-18)

### Bug Fixes

-  FKPOC-400 handleRegelRequest i separat framework för maskinell regel ([46bec](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/46bec6e92786281) Ulf Slunga)  

### Other changes

**regelService injectas i extendande klasser istället**


[bc4c3](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/bc4c3b89c76b11e) Ulf Slunga *2026-02-18 12:25:56*


## 0.2.2 (2026-02-17)

### Bug Fixes

-  Rename init -> initRegelRequestHandlerBase to avoid shadowing by child classes ([cf628](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/cf628e156981efc) Lars Persson)  

## 0.2.1 (2026-02-16)

### Bug Fixes

-  sendResponse och updateKundbehovsflodeInfo protected för reusability ([22aa0](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/22aa0558f99ca28) Ulf Slunga)  

## 0.2.0 (2026-02-16)

### Features

-  Update RegelRequestHandler and interface for rule implementation ([70dfb](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/70dfbb71db2eb56) Nils Elveros)  

### Bug Fixes

-  spotless ([80b49](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/80b4997079cce4f) Nils Elveros)  

### Other changes

**renamed class and removed applicationscope**


[36394](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/36394ba58fcc16a) Nils Elveros *2026-02-16 09:00:05*


## 0.1.12 (2026-02-13)

### Bug Fixes

-  bumpar kundbehovsflöde adapter för att fixa beslutsutfall null ([1f1f2](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/1f1f2a02a26ff2c) Ulf Slunga)  

## 0.1.11 (2026-02-12)

### Bug Fixes

-  removed unused import ([0b0a5](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/0b0a5744a76afcb) Nils Elveros)  
-  Add kundbehovsflode mapping and removed dependency towrads kundbehovsflode openapi ([0715f](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/0715f0b8455df1b) Nils Elveros)  

## 0.1.10 (2026-02-12)

### Bug Fixes

-  Readd classpath config discovery ([d4a1c](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/d4a1c77ba53ce6d) Lars Persson)  

## 0.1.9 (2026-02-12)

### Bug Fixes

-  UtokadUppgiftBeskrivning -> UtokadUppgiftsbeskrivning ([5f2ed](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/5f2ed008b5a5395) Lars Persson)  

## 0.1.8 (2026-02-11)

### Bug Fixes

-  Lägger till kundbehovsflöde adapter ([eb275](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/eb275eb72aad897) Ulf Slunga)  
-  lägger till schema för config-filen ([d0ee0](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/d0ee0526262849d) Ulf Slunga)  

## 0.1.7 (2026-02-11)

### Bug Fixes

-  Include UtokadUppgiftBeskrivning in RegelConfig ([1fe2e](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/1fe2ee6c9ac8034) Lars Persson)  

## 0.1.6 (2026-02-09)

### Bug Fixes

-  Lägger till regelhantering till ramverket ([4d28f](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/4d28f89b2a0e068) Ulf Slunga)  

## 0.1.5 (2026-02-09)

### Bug Fixes

-  Rework config discovery handling ([ba2df](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/ba2df031beb4faf) Lars Persson)  

## 0.1.4 (2026-02-09)

### Bug Fixes

-  Lägger till regel handler ([78d09](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/78d093fe028cbae) Ulf Slunga)  
-  README uppdatering ([25706](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/257060d31738258) Ulf Slunga)  

## 0.1.3 (2026-02-05)

### Bug Fixes

-  lägger till RegelData till ramverket ([cbef3](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/cbef311d0ead95c) Ulf Slunga)  

## 0.1.2 (2026-02-04)

### Bug Fixes

-  tar bort onödiga interface ([687d2](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/687d20b4432eed0) Ulf Slunga)  
-  Move kafka folder to presentation/kafka in order to match declared package ([0a829](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/0a82920ca641f66) Lars Persson)  

## 0.1.1 (2026-02-04)

### Bug Fixes

-  Skippa generic DTOs (behövs ej) ([538c3](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/538c31ab051b30a) Ulf Slunga)  

### Other changes

**spotless apply**


[cc261](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/cc261eb71ee2a0b) Ulf Slunga *2026-02-04 06:20:04*


## 0.1.0 (2026-02-02)

### Features

-  Add support for GET/PUT rest operations against kundbehovsflode ([4994c](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/4994c91dad9b1a7) Lars Persson)  

## 0.0.5 (2026-02-02)

### Bug Fixes

-  files in correct directory ([eaee6](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/eaee6ddb8366c5b) Nils Elveros)  
-  Replace regel.common imports with framework.regel imports ([cc137](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/cc137e57c41bae6) Lars Persson)  

## 0.0.4 (2026-01-30)

### Bug Fixes

-  update to groupid se.fk.rimfrost.framework.regel ([00448](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/00448a2b2808ab0) Nils Elveros)  

## 0.0.3 (2026-01-29)

### Bug Fixes

-  add source to cloudevent ([98b09](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/98b09d26a3aa0a0) Nils Elveros)  

## 0.0.2 (2026-01-27)

### Bug Fixes

-  RegelKafkaProducer saknades ([75bb0](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/75bb0abf28dd042) Ulf Slunga)  

## 0.0.1 (2026-01-27)

### Bug Fixes

-  endast bundle maven lib workflows ([ed14d](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/ed14ddd87c9dc56) Ulf Slunga)  
-  Lägger till maven projekt ([aef3a](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/aef3ad3713e0090) Ulf Slunga)  
-  Lägger till github workflow ([03ed7](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/03ed7aca6742c6c) Ulf Slunga)  

### Other changes

**spotless apply**


[55b56](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/55b5605bd855ed5) Ulf Slunga *2026-01-27 10:26:53*

**Anväd regelns unika topic som type i kogito-meddelande**


[31a40](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/31a40b3545d9aed) Ulf Slunga *2026-01-27 10:19:53*

**Använd regelns unika topic som type i kogito-meddelande**


[464f9](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/464f9ac37370abe) Ulf Slunga *2026-01-27 10:12:07*

**uppdaterar RegelConfig från rimfrost-common**


[f2e6c](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/f2e6c34ee5c6947) Ulf Slunga *2026-01-27 07:23:11*

**spotless apply**


[78939](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/7893966948dbede) Ulf Slunga *2026-01-27 07:01:48*

**Utfall ist för RattTillForsakring**


[dbb6a](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/dbb6af00e30cc8f) Ulf Slunga *2026-01-27 06:54:18*

**spotless apply**


[f9497](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/f949777099be282) Ulf Slunga *2026-01-27 05:29:51*

**Config file**


[dcc40](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/dcc408de7a803c8) Ulf Slunga *2026-01-26 13:22:44*

**Implementation kopierad från rimfrost-common**


[caf94](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/caf94b82a9ac4cb) Ulf Slunga *2026-01-26 13:22:44*

**Create CODEOWNERS**


[1358e](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/1358e69058c72e3) Ulf Slunga *2026-01-26 13:13:50*

**Create maven-release.yaml**


[38570](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/38570055a4825a2) Ulf Slunga *2026-01-26 13:13:02*

**Create maven-ci.yaml**


[5e9ff](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/5e9ff37a0f943dc) Ulf Slunga *2026-01-26 13:12:28*

**Initial commit**


[9b433](https://github.com/Forsakringskassan/rimfrost-framework-regel/commit/9b433f4fb73b633) Ulf Slunga *2026-01-26 11:58:29*


