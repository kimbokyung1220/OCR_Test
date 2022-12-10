#### 🍃 SpringBoot -version '2.7.5'
#### 🌏 java -version 11
#### 🐘 radle
#### ☁️ Google Cloud Vision API - OCR

### 🔍 Introduce  
> #### 미니프로젝트에서 명함이미지 내 텍스트를 추출하는 기능을 맡게되어 OCR기능을 구현해봤습니다.
> #### 사용한 API는 Google Vision Cloude입니다.
> #### 추출가능한 텍스트는 휴대폰번호, 기업번호, 팩스번호, 이메일입니다.


### 🔸 명함 가이드
![명함샘플2](https://user-images.githubusercontent.com/58963042/206863101-2fcad11b-4986-4fe5-99eb-c7e6a2ec89c3.png)

#### ✔ 번호의 형식은 하이픈("-"), 공백(" "), 점("."), 점+공백(". ")의 네가지 경우만 가능합니다.
####    ex) 010-0000-0000 / 010 0000 0000 / 010.0000.0000 / 010. 0000. 0000
#### ✔ 휴대폰번호를 제외한 기업번호, 팩스번호는 번호 앞에 어떤 번호를 나타내는 것인지에 대한 정보가 필요합니다.
####   (* 단 제공하고있는 양식에 맞는 정보여야만 인식 가능합니다)
#### ✔ 휴대폰번호는 +82 또는 82의 양식이 가능합니다.
####   ex) 82-01-0000-0000


+ 휴대폰 번호의 경우
  + Mobile. / Mobile / Mob. / Mob. / M. / M / m. / m  또는 없어도 가능
+ 기업번호의 경우
  + TEL. / TEL / Tel. / Tel. / tel. / tel / T. / T / t. / t 
+ 팩스번호의 경우
  + FAX. / FAX / Fax. / Fax. / fax. / fax / F. / F / f. / f
+ 이메일의 경우
  + E-mail. / E-mail / Email. / Email. / E. / E 
  + @이 포함되어야함
  + .com / .kr / .net



####  📃 패키지 및 클래스는 이렇습니다!
![image](https://user-images.githubusercontent.com/58963042/202008826-012cea8e-8200-49b6-90ac-0911850ff5d5.png)
#### ❗❗ application.propertise 필요해요!
#### ❗❗ Google Vision Cloude - key.json 필요해요!

#### 🔖 참고TSTORY: https://bkyungkeem.tistory.com/42


