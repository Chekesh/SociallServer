package org.example;

import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

public class Database {
    protected String dbHost = "localhost";
    protected String dbPort = "3306";
    protected String dbUser = "root";
    protected String dbPass = "1234";//"MA58sh62.";
    protected String dbName = "socservices";

    String answerbd = null;
    Connection dbConnection;
    {
        try {
            dbConnection = getDbConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String request;

    /*Database(){
        try {
            dbConnection = getDbConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }*/

    public Connection getDbConnection()
            throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + dbHost + ":"
                + dbPort + "/" + dbName;
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);
        System.out.println("Все заебок");
        return dbConnection;
    }

    /*public String CheckUser(String snils, String salt_password) {
        request = "SELECT * FROM user_profile WHERE snils = ? AND salt_password = ?";
        try {
            PreparedStatement prSt = dbConnection.prepareStatement(request);
            prSt.setString(1, snils);
            prSt.setString(2, salt_password);
            ResultSet resultSet = prSt.executeQuery();
            int schet = 0;
            while (resultSet.next()) {
                answerbd =  "OK";
                schet = 1;
            }
            if (schet == 0) {
                answerbd = "BADLY";
            }
            else if (schet > 1){
                answerbd = "Вы что ебланы как получилось 2 или больше одинаковых?";
            }
            return answerbd;

        } catch (SQLException e) {
            System.out.println("ошибка ");
            throw new RuntimeException(e);
        }
    }*/

    //немножка переписала метод сверху, добавила проверку
    //Чет не работает
    public String CheckUser(String snils, String enteredPassword) {
        request = "SELECT hashed_password, salt_password FROM user_profile WHERE snils = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, snils);
            ResultSet resultSet = prSt.executeQuery();

            int schet = 0;
            String answerbd = null;

            while (resultSet.next()) {
                String hashedPasswordFromDB = resultSet.getString("hashed_password");
                String saltFromDB = resultSet.getString("salt_password");

                // Проверка пароля
                if (BCrypt.checkpw(enteredPassword, hashedPasswordFromDB)) {
                    answerbd = "OK";
                    schet++;
                }
            }

            if (schet == 0) {
                answerbd = "BADLY";
            } else if (schet > 1) {
                answerbd = "Вы что ебланы как получилось 2 или больше одинаковых?"; //главное не забыть убрать
            }

            return answerbd;

        } catch (SQLException e) {
            System.out.println("ошибка");
            throw new RuntimeException(e);
        }
    }
    //проверка работника
    public String checkWorkerCredentials(String login, String password) {
        request = "SELECT hashed_password, salt_password FROM worker_profile WHERE login = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, login);
            ResultSet resultSet = prSt.executeQuery();
            if (resultSet.next()) {
                String hashedPasswordFromDB = resultSet.getString("hashed_password");
                String saltFromDB = resultSet.getString("salt_password");

                // Проверка пароля
                if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                    answerbd = "OK";
                }
                else {
                    answerbd = "BADLY";
                }
            }
            return answerbd;
        } catch (SQLException e) {
            answerbd = "Ошибка при проверке учетных данных работника.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
    }
    //метод для регистрации пользователя с хешированным паролем и солью
    public String registerUser(String snils, String phone, String plainPassword) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
        request = "INSERT INTO user_profile (snils, phone, hashed_password, salt_password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, snils);
            prSt.setString(2, phone);
            prSt.setString(3, hashedPassword);
            prSt.setString(4, salt);

            prSt.executeUpdate();
            answerbd = "Пользователь успешно зарегистрирован.";
            System.out.println(answerbd);
        } catch (SQLException e) {
            answerbd = "Ошибка при регистрации пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //метод для регистрации работника с хешированным паролем и солью
    public String registerWorker(String login, String plainPassword, String name, String surname, String patronymic, String post, boolean isAdmin) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);

        request = "INSERT INTO worker_profile (login, hashed_password, salt_password, name, surname, patronymic, post, admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, login);
            prSt.setString(2, hashedPassword);
            prSt.setString(3, salt);
            prSt.setString(4, name);
            prSt.setString(5, surname);
            prSt.setString(6, patronymic);
            prSt.setString(7, post);
            prSt.setBoolean(8, isAdmin);

            prSt.executeUpdate();
            answerbd = "Работник успешно зарегистрирован.";
            System.out.println(answerbd);

        } catch (SQLException e) {
            answerbd = "Ошибка при регистрации работника.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //Я пока не разобралась что такое эдюзер и с чем его едят
    public String addUser(String snils, String documentName, String documentNumber,
                          String name, String surname, String patronymic,
                          Date birthdate, String phone, String email, //?
                          int regionId, String regionSmall,
                          String city, String street, String home, String apartment) {
        request = "INSERT INTO user_data (snils, document_name, document_number, name, surname, patronymic, brithdate, phone, email, region_id, region_small, city, street, home, apartment) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, snils);
            prSt.setString(2, documentName);
            prSt.setString(3, documentNumber);
            prSt.setString(4, name);
            prSt.setString(5, surname);
            prSt.setString(6, patronymic);
            prSt.setDate(7, birthdate);
            prSt.setString(8, phone);
            prSt.setString(9, email);
            prSt.setInt(10, regionId);
            prSt.setString(11, regionSmall);
            prSt.setString(12, city);
            prSt.setString(13, street);
            prSt.setString(14, home);
            prSt.setString(15, apartment);

            prSt.executeUpdate();
            answerbd = "Пользователь успешно добавлен в таблицу user_data.";
            System.out.println(answerbd);

        } catch (SQLException e) {
            answerbd = "Ошибка при добавлении пользователя в таблицу user_data.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //заявление?
    public String addApplication(String userSnils, Integer userAddId, Integer workerProfileId,
                                 int socOrganizationOgrrn, String form, String reason,
                                 boolean domestic, boolean medical, boolean psychological,
                                 boolean pedagogical, boolean labour, boolean legal,
                                 boolean communication, boolean urgent, String family,
                                 String living, int income, String status) {
        request = "INSERT INTO application (user_data_snils, user_data_add_, worker_profile_id, soc_organization_ogrrn, form, reason, " +
                "domestic, medical, psychological, pedagogical, labour, legal, communication, urgent, famaly, living, income, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, userSnils);
            prSt.setObject(2, userAddId);  // Может быть NULL, поэтому используем setObject
            prSt.setObject(3, workerProfileId);  // Может быть NULL, поэтому используем setObject
            prSt.setInt(4, socOrganizationOgrrn);
            prSt.setString(5, form);
            prSt.setString(6, reason);
            prSt.setBoolean(7, domestic);
            prSt.setBoolean(8, medical);
            prSt.setBoolean(9, psychological);
            prSt.setBoolean(10, pedagogical);
            prSt.setBoolean(11, labour);
            prSt.setBoolean(12, legal);
            prSt.setBoolean(13, communication);
            prSt.setBoolean(14, urgent);
            prSt.setString(15, family);
            prSt.setString(16, living);
            prSt.setInt(17, income);
            prSt.setString(18, status);

            prSt.executeUpdate();
            System.out.println(answerbd);
            answerbd = "Заявка успешно добавлена в таблицу ";
        } catch (SQLException e) {
            answerbd = "Ошибка при добавлении заявки в таблицу ";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //форма
    public String addForm(String name) {
        request = "INSERT INTO form (name) VALUES (?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, name);

            prSt.executeUpdate();
            answerbd = "Форма успешно добавлена в таблиц";
            System.out.println(answerbd);

        } catch (SQLException e) {
            answerbd = "Ошибка при добавлении формы в таблицу";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    // Метод для добавления новой социальной организации
    public String addSocOrganization(int ogrrn, String name) {
        request = "INSERT INTO soc_organization (ogrrn, name) VALUES (?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, ogrrn);
            prSt.setString(2, name);

            prSt.executeUpdate();
            answerbd = "Социальная организация успешно добавлена в таблицу";
            System.out.println(answerbd);

        } catch (SQLException e) {
            answerbd = "Ошибка при добавлении социальной организации в таблицу";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    // Метод для добавления нового документа
    public String addDocument(String name, String regex) {
        request = "INSERT INTO document (name, regex) VALUES (?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, name);
            prSt.setString(2, regex);

            prSt.executeUpdate();
            answerbd = "Документ успешно добавлен в таблицу document.";
            System.out.println(answerbd);
        } catch (SQLException e) {
            answerbd = "Ошибка при добавлении документа в таблицу document.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    // Метод для добавления нового региона
    public String addRegion(int id, String name) {
        request = "INSERT INTO region (id, name) VALUES (?, ?)";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, id);
            prSt.setString(2, name);

            prSt.executeUpdate();
            answerbd = "Регион успешно добавлен в таблицу";
            System.out.println(answerbd);
        } catch (SQLException e) {
            answerbd = "Ошибка при добавлении региона в таблицу";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление работника
    public String deleteWorker(int workerId) {
        request = "DELETE FROM worker_profile WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, workerId);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Работник успешно удален.";
                System.out.println(answerbd);
            } else {
                answerbd = "Работник с указанным ID не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении работника.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление пользователя
    public String deleteUser(String snils) {
        request = "DELETE FROM user_profile WHERE snils = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, snils);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Пользователь успешно удален.";
                System.out.println(answerbd);
            } else {
                answerbd = "Пользователь с указанным СНИЛС не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление документа
    public String deleteDocument(String documentName) {
        request = "DELETE FROM document WHERE name = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, documentName);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Документ успешно удален.";
                System.out.println(answerbd);
            } else {
                answerbd = "Документ с указанным именем не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении документа.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление региона
    public String deleteRegion(int regionId) {
        String request = "DELETE FROM region WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, regionId);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Регион успешно удален.";
                System.out.println(answerbd);
            } else {
                answerbd = "Регион с указанным ID не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении региона.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление данных пользователя
    public String deleteUserData(String snils) {
        String request = "DELETE FROM user_data WHERE snils = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, snils);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Данные пользователя успешно удалены.";
                System.out.println(answerbd);
            } else {
                answerbd = "Пользователь с указанным СНИЛС не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении данных пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //
    public String deleteUserAdditionalData(int dataId) {
        String request = "DELETE FROM user_data_add WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, dataId);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Дополнительные данные пользователя успешно удалены.";
                System.out.println(answerbd);
            } else {
                answerbd = "Данные с указанным ID не найдены.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении дополнительных данных пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление организации
    public String deleteSocialOrganization(int ogrrn) {
        String request = "DELETE FROM soc_organization WHERE ogrrn = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, ogrrn);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Социальная организация успешно удалена.";
                System.out.println(answerbd);
            } else {
                answerbd = "Организация с указанным OGRRN не найдена.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении социальной организации.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //удаление формы
    public String deleteForm(String formName) {
        String request = "DELETE FROM form WHERE name = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, formName);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Форма успешно удалена.";
                System.out.println(answerbd);
            } else {
                answerbd = "Форма с указанным именем не найдена.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении формы.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
    //заявление удаление
    public String deleteApplication(int applicationId) {
        String request = "DELETE FROM application WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setInt(1, applicationId);

            int rowsDeleted = prSt.executeUpdate();

            if (rowsDeleted > 0) {
                answerbd = "Заявление успешно удалено.";
                System.out.println(answerbd);
            } else {
                answerbd = "Заявление с указанным ID не найдено.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при удалении заявления.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateWorkerProfile(int id, String login, String plainPassword, String name, String surname, String patronymic, String post, int admin) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
        String request = "UPDATE worker_profile SET login = ?, hashed_password = ?, salt_password = ?, name = ?, surname = ?, patronymic = ?, post = ?, admin = ? WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, login);
            prSt.setString(2, hashedPassword);
            prSt.setString(3, salt);
            prSt.setString(4, name);
            prSt.setString(5, surname);
            prSt.setString(6, patronymic);
            prSt.setString(7, post);
            prSt.setInt(8, admin);
            prSt.setInt(9, id);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Профиль работника успешно обновлен.";
                System.out.println(answerbd);
            } else {
                answerbd = "Работник с указанным ID не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении профиля работника.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateUserProfile(String snils, String phone, String plainPassword) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
        String request = "UPDATE user_profile SET phone = ?, hashed_password = ?, salt_password = ? WHERE snils = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, phone);
            prSt.setString(2, hashedPassword);
            prSt.setString(3, salt);
            prSt.setString(4, snils);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Профиль пользователя успешно обновлен.";
                System.out.println(answerbd);
            } else {
                answerbd = "Пользователь с указанным СНИЛС не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении профиля пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateDocument(String name, String newRegex) {
        String request = "UPDATE document SET regex = ? WHERE name = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, newRegex);
            prSt.setString(2, name);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Документ успешно обновлен.";
                System.out.println(answerbd);
            } else {
                answerbd = "Документ с указанным именем не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении документа.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateRegion(int id, String name) {
        String request = "UPDATE region SET name = ? WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, name);
            prSt.setInt(2, id);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Регион успешно обновлен.";
                System.out.println(answerbd);
            } else {
                answerbd = "Регион с указанным ID не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении региона.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateUserData(String snils, String documentName, String documentNumber, String name, String surname, String patronymic,
                                 Date birthdate, String phone, String email, int regionId, String regionSmall, String city, String street,
                                 String home, String apartment) { //!
        String request = "UPDATE user_data SET document_name = ?, document_number = ?, name = ?, surname = ?, patronymic = ?, brithdate = ?, " +
                "phone = ?, email = ?, region_id = ?, region_small = ?, city = ?, street = ?, home = ?, apartment = ? " +
                "WHERE snils = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, documentName);
            prSt.setString(2, documentNumber);
            prSt.setString(3, name);
            prSt.setString(4, surname);
            prSt.setString(5, patronymic);
            prSt.setDate(6, birthdate);
            prSt.setString(7, phone);
            prSt.setString(8, email);
            prSt.setInt(9, regionId);
            prSt.setString(10, regionSmall);
            prSt.setString(11, city);
            prSt.setString(12, street);
            prSt.setString(13, home);
            prSt.setString(14, apartment);
            prSt.setString(15, snils);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Данные пользователя успешно обновлены.";
                System.out.println(answerbd);
            } else {
                answerbd = "Пользователь с указанным СНИЛС не найден.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении данных пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateUserDataAdd(int id, String documentName, String documentNumber, String name, int addressRegionId, String addressSmallRegion,
                                    String addressCity, String addressStreet, String addressHome, int addressApartment) {
        String request = "UPDATE user_data_add SET document_name = ?, document_number = ?, name = ?, address_region_id = ?, " +
                "address_small_region = ?, address_city = ?, address_street = ?, address_home = ?, address_apartment = ? " +
                "WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, documentName);
            prSt.setString(2, documentNumber);
            prSt.setString(3, name);
            prSt.setInt(4, addressRegionId);
            prSt.setString(5, addressSmallRegion);
            prSt.setString(6, addressCity);
            prSt.setString(7, addressStreet);
            prSt.setString(8, addressHome);
            prSt.setInt(9, addressApartment);
            prSt.setInt(10, id);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Дополнительные данные пользователя успешно обновлены.";
                System.out.println(answerbd);
            } else {
                answerbd = "Данные с указанным ID не найдены.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении дополнительных данных пользователя.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateSocOrganization(int ogrrn, String newName) {
        String request = "UPDATE soc_organization SET name = ? WHERE ogrrn = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, newName);
            prSt.setInt(2, ogrrn);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Социальная организация успешно обновлена.";
                System.out.println(answerbd);
            } else {
                answerbd = "Организация с указанным OGRRN не найдена.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении социальной организации.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateForm(String currentName, String newName) {
        String request = "UPDATE form SET name = ? WHERE name = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, newName);
            prSt.setString(2, currentName);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Форма успешно обновлена.";
                System.out.println(answerbd);
            } else {
                answerbd = "Форма с указанным именем не найдена.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении формы.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }

    public String updateApplication(int id, String userDataSnils, int userDataAddId, int workerProfileId,
                                    int socOrganizationOgrrn, String form, String reason, boolean domestic, boolean medical,
                                    boolean psychological, boolean pedagogical, boolean labour, boolean legal, boolean communication,
                                    boolean urgent, String famaly, String living, int income, String status) {
        String request = "UPDATE application SET user_data_snils = ?, user_data_add_ = ?, worker_profile_id = ?, " +
                "soc_organization_ogrrn = ?, form = ?, reason = ?, domestic = ?, medical = ?, psychological = ?, " +
                "pedagogical = ?, labour = ?, legal = ?, communication = ?, urgent = ?, famaly = ?, living = ?, " +
                "income = ?, status = ? WHERE id = ?";

        try (PreparedStatement prSt = dbConnection.prepareStatement(request)) {
            prSt.setString(1, userDataSnils);
            prSt.setObject(2, userDataAddId, Types.INTEGER);
            prSt.setObject(3, workerProfileId, Types.INTEGER);
            prSt.setInt(4, socOrganizationOgrrn);
            prSt.setString(5, form);
            prSt.setString(6, reason);
            prSt.setBoolean(7, domestic);
            prSt.setBoolean(8, medical);
            prSt.setBoolean(9, psychological);
            prSt.setBoolean(10, pedagogical);
            prSt.setBoolean(11, labour);
            prSt.setBoolean(12, legal);
            prSt.setBoolean(13, communication);
            prSt.setBoolean(14, urgent);
            prSt.setString(15, famaly);
            prSt.setString(16, living);
            prSt.setInt(17, income);
            prSt.setString(18, status);
            prSt.setInt(19, id);

            int rowsUpdated = prSt.executeUpdate();

            if (rowsUpdated > 0) {
                answerbd = "Заявление успешно обновлено.";
                System.out.println(answerbd);
            } else {
                answerbd = "Заявление с указанным ID не найдено.";
                System.out.println(answerbd);
            }

        } catch (SQLException e) {
            answerbd = "Ошибка при обновлении заявления.";
            System.out.println(answerbd);
            throw new RuntimeException(e);
        }
        finally {
            return answerbd;
        }
    }
}

