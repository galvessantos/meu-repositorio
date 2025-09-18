-- ******* ADMINISTRADOR *******
INSERT INTO permissions (action, subject, fields, description)
VALUES ('manage', 'all', NULL, 'Permissão para gerenciar tudo no sistema');


-------------------------------------------------------------------------------------------------
-- ******* ENDERECO *******
-- Permissão para leitura de endereços
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'Address', 'id,postalCode,street,number,neighborhood,state,city,complement,note', 'Permissão para ler endereços');

-- Permissão para criação de endereços
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'Address', 'postalCode,street,number,neighborhood,state,city,complement,note', 'Permissão para criar novos endereços');

-- Permissão para atualização de endereços
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'Address', 'postalCode,street,number,neighborhood,state,city,complement,note', 'Permissão para atualizar endereços existentes');

-- Permissão para exclusão de endereços
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'Address', NULL, 'Permissão para excluir endereços');


-------------------------------------------------------------------------------------------------
-- ******* EMPRESA *******
-- Permissão para leitura de empresas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'Company', 'id,name,email,document,phoneDDD,phoneNumber,phoneType,address,nameResponsible,companyType,isActive,createdAt', 'Permissão para ler empresas');

-- Permissão para criação de empresas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'Company', 'name,email,document,phoneDDD,phoneNumber,phoneType,address,nameResponsible,companyType,isActive', 'Permissão para criar novas empresas');

-- Permissão para atualização de empresas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'Company', 'name,email,document,phoneDDD,phoneNumber,phoneType,address,nameResponsible,companyType,isActive', 'Permissão para atualizar empresas existentes');

-- Permissão para exclusão de empresas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'Company', NULL, 'Permissão para excluir empresas');

-------------------------------------------------------------------------------------------------
-- ******* DEVEDOR *******
-- Permissão para leitura de devedores
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'Debtor', 'id,name,cpfCnpj,addressId', 'Permissão para ler informações de devedores');

-- Permissão para criação de devedores
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'Debtor', 'name,cpfCnpj,addressId', 'Permissão para criar novos registros de devedores');

-- Permissão para atualização de devedores
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'Debtor', 'name,cpfCnpj,addressId', 'Permissão para atualizar registros de devedores existentes');

-- Permissão para exclusão de devedores
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'Debtor', NULL, 'Permissão para excluir registros de devedores');

-------------------------------------------------------------------------------------------------
-- ******* RELATÓRIO DE ARQUIVOS *******
-- Permissão para leitura de relatórios de arquivos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'FileReport', 'id,receivedDate,fileName,fileType,filePath,reportId', 'Permissão para ler relatórios de arquivos');

-- Permissão para criação de relatórios de arquivos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'FileReport', 'receivedDate,fileName,fileType,filePath,reportId,pdfData', 'Permissão para criar novos relatórios de arquivos');

-- Permissão para atualização de relatórios de arquivos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'FileReport', 'receivedDate,fileName,fileType,filePath,reportId,pdfData', 'Permissão para atualizar relatórios de arquivos existentes');

-- Permissão para exclusão de relatórios de arquivos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'FileReport', NULL, 'Permissão para excluir relatórios de arquivos');

-------------------------------------------------------------------------------------------------
-- ******* HISTÓRICO *******
-- Permissão para leitura de históricos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'History', 'id,idVehicle,licensePlate,model,creditorName,contractNumber,creationDateTime,typeHistory,location,collected,impoundLot', 'Permissão para ler registros de histórico');

-- Permissão para criação de históricos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'History', 'idVehicle,licensePlate,model,creditorName,contractNumber,creationDateTime,typeHistory,location,collected,impoundLot', 'Permissão para criar novos registros de histórico');

-- Permissão para atualização de históricos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'History', 'idVehicle,licensePlate,model,creditorName,contractNumber,creationDateTime,typeHistory,location,collected,impoundLot', 'Permissão para atualizar registros de histórico existentes');

-- Permissão para exclusão de históricos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'History', NULL, 'Permissão para excluir registros de histórico');


-------------------------------------------------------------------------------------------------
-- ******* RELATÓRIO *******
-- Permissão para leitura de relatórios
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'ReportCollection', 'id,seizureDateId,mandateNumber,mandateDate,witnessesId,contractID,contractNumber,debtValue,towTruckId,notificationId,arNotificationId', 'Permissão para ler registros de relatórios');

-- Permissão para criação de relatórios
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'ReportCollection', 'seizureDateId,mandateNumber,mandateDate,witnessesId,contractID,contractNumber,debtValue,towTruckId,notificationId,arNotificationId', 'Permissão para criar novos registros de relatórios');

-- Permissão para atualização de relatórios
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'ReportCollection', 'seizureDateId,mandateNumber,mandateDate,witnessesId,contractID,contractNumber,debtValue,towTruckId,notificationId,arNotificationId', 'Permissão para atualizar registros de relatórios existentes');

-- Permissão para exclusão de relatórios
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'ReportCollection', NULL, 'Permissão para excluir registros de relatórios');

-------------------------------------------------------------------------------------------------
-- ******* DATA DE APREENSÃO *******
-- Permissão para leitura de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'SeizureDate', 'id,veiculeId,seizureDate,createdAt', 'Permissão para ler registros de datas de apreensão');

-- Permissão para criação de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'SeizureDate', 'veiculeId,seizureDate', 'Permissão para criar novos registros de datas de apreensão');

-- Permissão para atualização de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'SeizureDate', 'veiculeId,seizureDate', 'Permissão para atualizar registros de datas de apreensão existentes');

-- Permissão para exclusão de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'SeizureDate', NULL, 'Permissão para excluir registros de datas de apreensão');

-------------------------------------------------------------------------------------------------
-- ******* DATA DE APREENSÃO *******
-- Permissão para leitura de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'SeizureDate', 'id,veiculeId,seizureDate,createdAt', 'Permissão para ler registros de datas de apreensão');

-- Permissão para criação de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'SeizureDate', 'veiculeId,seizureDate', 'Permissão para criar novos registros de datas de apreensão');

-- Permissão para atualização de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'SeizureDate', 'veiculeId,seizureDate', 'Permissão para atualizar registros de datas de apreensão existentes');

-- Permissão para exclusão de datas de apreensão
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'SeizureDate', NULL, 'Permissão para excluir registros de datas de apreensão');

-------------------------------------------------------------------------------------------------
-- ******* ASSOCIAÇÃO USUÁRIO-VEÍCULO *******
-- Permissão para leitura de associações usuário-veículo
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'UserVehicleAssociation', 'id,userId,vehicleId,associatedBy,createdAt', 'Permissão para ler registros de associações usuário-veículo');

-- Permissão para criação de associações usuário-veículo
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'UserVehicleAssociation', 'userId,vehicleId,associatedBy,createdAt', 'Permissão para criar novos registros de associações usuário-veículo');

-- Permissão para atualização de associações usuário-veículo
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'UserVehicleAssociation', 'userId,vehicleId,associatedBy,createdAt', 'Permissão para atualizar registros de associações usuário-veículo existentes');

-- Permissão para exclusão de associações usuário-veículo
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'UserVehicleAssociation', NULL, 'Permissão para excluir registros de associações usuário-veículo');

-------------------------------------------------------------------------------------------------
-- ******* VEÍCULO *******
-- Permissão para leitura de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'Vehicle', 'id,licensePlate,model,registrationState,creditorName,contractNumber,stage,status,requestDate,vehicleSeizureDateTime,lastMovementDate,seizureScheduledDate', 'Permissão para ler registros de veículos');

-- Permissão para criação de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'Vehicle', 'licensePlate,model,registrationState,creditorName,contractNumber,stage,status,requestDate,vehicleSeizureDateTime,lastMovementDate,seizureScheduledDate', 'Permissão para criar novos registros de veículos');

-- Permissão para atualização de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'Vehicle', 'licensePlate,model,registrationState,creditorName,contractNumber,stage,status,requestDate,vehicleSeizureDateTime,lastMovementDate,seizureScheduledDate', 'Permissão para atualizar registros de veículos existentes');

-- Permissão para exclusão de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'Vehicle', NULL, 'Permissão para excluir registros de veículos');

-------------------------------------------------------------------------------------------------
-- ******* APREENSÃO DE VEÍCULOS *******
-- Permissão para leitura de apreensões de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'VehicleSeizure', 'id,userId,vehicleId,addressId,companyId,vehicleCondition,seizureDate,createdAt,description,status', 'Permissão para ler registros de apreensões de veículos');

-- Permissão para criação de apreensões de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'VehicleSeizure', 'userId,vehicleId,addressId,companyId,vehicleCondition,seizureDate,description,status', 'Permissão para criar novos registros de apreensões de veículos');

-- Permissão para atualização de apreensões de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'VehicleSeizure', 'userId,vehicleId,addressId,companyId,vehicleCondition,seizureDate,description,status', 'Permissão para atualizar registros de apreensões de veículos existentes');

-- Permissão para exclusão de apreensões de veículos
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'VehicleSeizure', NULL, 'Permissão para excluir registros de apreensões de veículos');

-------------------------------------------------------------------------------------------------
-- ******* TESTEMUNHAS *******
-- Permissão para leitura de testemunhas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('read', 'WitnessesCollection', 'id,name,rg,cpf', 'Permissão para ler registros de testemunhas');

-- Permissão para criação de testemunhas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('create', 'WitnessesCollection', 'name,rg,cpf', 'Permissão para criar novos registros de testemunhas');

-- Permissão para atualização de testemunhas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('update', 'WitnessesCollection', 'name,rg,cpf', 'Permissão para atualizar registros de testemunhas existentes');

-- Permissão para exclusão de testemunhas
INSERT INTO permissions (action, subject, fields, description)
VALUES 
('delete', 'WitnessesCollection', NULL, 'Permissão para excluir registros de testemunhas');


