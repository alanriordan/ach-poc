package com.alan.sample.script

import groovy.sql.Sql


Sql db = Sql.newInstance(
	'jdbc:mysql://localhost:3306/poc',
	'root',
	'root',
	'com.mysql.jdbc.Driver')

db.execute 'drop table  if exists File'
db.execute 'drop table  if exists Batch'
db.execute 'drop table  if exists Transaction'


db.execute('''
    create table File(
        id int not null auto_increment,
        msg_id varchar(32) not null,
        credit_time varchar(32) not null,
        num_transactions int not null,
        ctrl_sum varchar(32) not null,
        init_party_private_id varchar(32),        
        primary key(id)
    );	
''')

db.execute('''
	create table Batch(
		id int not null auto_increment,
        payment_info_id varchar(32) not null,
        payment_method varchar(32) not null,
        num_of_Transactions int not null,
        payment_ctrl_sum varchar(32) not null,
        service_level_Code varchar(32),
        local_inst_Code varchar(32),
		sequence_type varchar(32), 
		collection_date varchar(32), 
		creditor_name varchar(32), 
		creditor_acc_iban varchar(32), 
		creditor_bic varchar(32), 
		creditor_scheme_id varchar(32), 
		creditor_scheme_name varchar(32),         
        primary key(id)
	);
''')

db.execute('''
	create table Transaction(
		id int not null auto_increment,
        end_to_end_id varchar(80) not null,
        instructed_amount varchar(32) not null,
        instructed_cur varchar(32) not null,
        mandate_id varchar(32) not null,
        mandate_date_sig varchar(32),  
		debit_agent_bnk_id varchar(32),
		debitor_name varchar(32),
		debitor_bank_acc_num varchar(32), 
		batch_id varchar(32),   
        primary key(id)
	);
''')



def records = new XmlParser().parse(new File("test-files/Pain008-2.xml"));

def root = records.CstmrDrctDbtInitn;
def header = root.GrpHdr;

def msgId = header.MsgId.text();
def creditTime = header.CreDtTm.text();
def numOfTxs = header.NbOfTxs.text();
def ctrlSum = header.CtrlSum.text();
def id = header.InitgPty.Id.PrvtId.Othr.Id.text();

db.execute("""insert into File values(1, $msgId,  $creditTime,  $numOfTxs, $ctrlSum, $id )""")
System.out.println(" $msgId  $creditTime  $numOfTxs $ctrlSum $id ");

def batchId = 1;
def trnId = 1;

root.PmtInf.each{b -> 
	def paymentInfoId = b.PmtInfId.text();
	def paymentMethod = b.PmtMtd.text();
	def batchBooking = b.BtchBookg.text();
	def batchNumOfTxs = b.NbOfTxs.text();
	def paymentCtrlSum = b.CtrlSum.text();
	def serviceLevelCode = b.PmtTpInf.SvcLvl.Cd.text();
	def localInstCode  = b.PmtTpInf.LclInstrm.Cd.text();
	def sequenceType = b.PmtTpInf.SeqTp.text();
	def collectionDate = b.ReqdColltnDt.text();
	def creditorName = b.Cdtr.Nm.text();
	def creditorAccIban = b.CdtrAcct.Id.IBAN.text();
	def creditorBankIdentifer  = b.CdtrAgt.FinInstnId.BIC.text();
	def creditorSchemeIdentifer = b.CdtrSchmeId.Id.PrvtId.Othr.Id.text();
	def creditorSchemeName = b.CdtrSchmeId.Id.PrvtId.Othr.SchmeNm.Prtry.text();
	
	db.execute("""insert into Batch values($batchId, $paymentInfoId,  $paymentMethod,
         $batchNumOfTxs, $paymentCtrlSum, $serviceLevelCode, $localInstCode, $sequenceType,
         $collectionDate, $creditorName, $creditorAccIban, $creditorBankIdentifer, $creditorSchemeIdentifer,
         $creditorSchemeName)""")
	
	
	
	b.DrctDbtTxInf.each {t -> 
		def endToEndId = t.PmtId.EndToEndId.text();
		def instructedAmount = t.InstdAmt.text();
		def instructedCurrenty = t.InstdAmt.'@Ccy'.text();
		def mandateId = t.DrctDbtTx.MndtRltdInf.MndtId.text()
		def mandateDateOfSig = t.DrctDbtTx.MndtRltdInf.DtOfSgntr.text()
		def debitAgtBankIdentifer = t.DbtrAgt.FinInstnId.BIC.text()
		def debitorName = t.Dbtr.Nm.text()
		def debitorBankAccNum = t.DbtrAcct.Id.IBAN.text()
		
		
		db.execute("""insert into Transaction values($trnId, $endToEndId, $instructedAmount,
         $instructedCurrenty, $mandateId, $mandateDateOfSig, $debitAgtBankIdentifer,
          $debitorName, $debitorBankAccNum, $batchId)""")
		
		trnId++
		
	}
	batchId++
	
	
};




	


