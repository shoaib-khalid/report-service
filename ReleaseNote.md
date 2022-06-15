##################################################
# Version v.3.3.2| 15-June-2022
##################################################
### Code Changes:
1. Added New Field Voucher Type 
2. Added new field store voucher discount amount



##################################################
# Version v.3.3.1| 31-May-2022
##################################################
### Code Changes:
1. Voucher Discount - Bug Fixed

##################################################
# Version v.3.3.0| 31-May-2022
##################################################
### Code Changes:
1. Added New Field -Voucher Discount


##################################################
# Version v.3.2.10| 04-Feb-2022
##################################################
### Code Changes:
1. Total Calculation Number bug Fixed - Dashboard


##################################################
# Version v.3.2.9| 11-January-2022
##################################################
### Code Changes:
1. Dashboard Graph Endpoint


##################################################
# Version v.3.2.8| 10-January-2022
##################################################
### Code Changes:
1. Dashboard total sale - bug fixed

##################################################
# Version v.3.2.7| 04-January-2022
##################################################
### Code Changes:
1. Merchant-Detail-Daily-Sale - added fields in the response - store share value - bug fixed

##################################################
# Version v.3.2.6| 03-January-2022
##################################################
### Code Changes:
1. Merchant-Detail-Daily-Sale - added fields in the response - store share value - bug fixed

##################################################
# Version v.3.2.5| 03-January-2022
##################################################
### Code Changes:
1. Merchant-Detail-Daily-Sale - added fields in the response - bug fixed

##################################################
# Version v.3.2.4| 29-December-2021
##################################################
### Code Changes:
1. Detail-Daily-Sale - old merchant portal endpoint - bug fixed 

##################################################
# Version v.3.2.3| 29-December-2021
##################################################
### Code Changes:
1. TotalSales count api bug fixed - removed date
2. Added new endpoint for dashboard - weekly sale by FE date range


##################################################
# Version v.3.2.2| 28-December-2021
##################################################
### Code Changes:
1. TotalSales count api bug fixed 


##################################################
# Version v.3.2.1| 28-December-2021
##################################################
### Code Changes:
1. Added sort by both value for product ranking endpoint


##################################################
# Version v.3.2.0| 28-December-2021
##################################################
### Code Changes:

1. Self Delivery Fee, Delivery Discount an Applied Discount added to the Daily sale and settlement.

   ALTER TABLE symplified.store_daily_sale ADD totalAppliedDiscount decimal(15,2) NULL;
   ALTER TABLE symplified.store_daily_sale ADD totalDeliveryDiscount decimal(15,2) NULL;
   ALTER TABLE symplified.store_daily_sale ADD totalSelfDeliveryFee decimal(15,2) NULL;


   ALTER TABLE symplified.store_settlement ADD totalSelfDeliveryFee decimal(15,2) NULL;
   ALTER TABLE symplified.store_settlement ADD totalAppliedDiscount decimal(15,2) NULL;
   ALTER TABLE symplified.store_settlement ADD totalDeliveryDiscount decimal(10,2) NULL;

2. Update Store Procedures for *insertDailySale*

"   CREATE DEFINER=`root`@`localhost` PROCEDURE `symplified`.`insertDailySales`()
   BEGIN

   DECLARE deliveryTypes varchar(50) ;
   DECLARE i int ;
   DECLARE totalType int ;
   DECLARE endDate timestamp;



   INSERT INTO store_daily_sale(`date`, storeId, totalOrders, 
   amountEarned, commision,totalServiceCharge,
   totalAmount,totalAppliedDiscount, totalDeliveryDiscount) 
   SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0) , 
   IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) ,  IFNULL(SUM(o.total),0), 
   IFNULL(SUM(o.appliedDiscount),0),IFNULL(SUM(o.deliveryDiscount),0)
   FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
   WHERE 
   `created` IS NOT NULL
   AND storeId IS NOT NULL 
   #AND DATE(created)=DATE(NOW()) 
   AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
   GROUP BY DATE(created), storeId
   ORDER BY DATE(created)
   ON DUPLICATE KEY UPDATE
   storeId=storeId;	

   UPDATE symplified.store_daily_sale sds
   SET sds.totalSelfDeliveryFee =(SELECT IFNULL(SUM(o.deliveryCharges),0) 		
   FROM symplified.`order` o  WHERE 
   o.deliveryType ="SELF" AND
   `created` > CONCAT(sds.`date`, ' 00:00:00') AND created < CONCAT(sds.`date`, ' 23:59:59')  
   AND storeId = sds.storeId AND o.paymentStatus = "PAID"), 
   
   sds.totalDeliveryFee =(SELECT IFNULL(SUM(o.deliveryCharges),0) 		
   FROM symplified.`order` o  WHERE 
   o.deliveryType != "SELF" AND
   `created` > CONCAT(sds.`date`, ' 00:00:00') AND created < CONCAT(sds.`date`, ' 23:59:59')  
   AND storeId = sds.storeId AND o.paymentStatus = "PAID");

   END "



##################################################
# Version v.3.1.1 | 20-December-2021
##################################################
### Code Changes:

1. Add Product_daily_sale table add **_Ranking_** column

Update DB insertProductDailySales **procedure**
UPDATE product_daily_sale r
SET r.ranking =(SELECT ranking FROM (SELECT a.`date`, a.productId,a.totalOrders, a.storeId, ROW_NUMBER() OVER(PARTITION BY a.`date` ,a.storeId ORDER by a.totalOrders desc  )as ranking
FROM symplified.product_daily_sale a ORDER by a.`date`  ,ranking ASC ) list WHERE list.productId = r.productId and list.`date` = r.`date`);

##################################################
# Version v.3.1.0 | 13-December-2021
##################################################
### Code Changes:

1. Bug fixed get **DetailDailySale** endpoint 


##################################################
# Version v.3.0.9 | 03-December-2021
##################################################
### Code Changes:

1. Bug fixed get **MerchantDailyTopProducts** endpoint and pagination


##################################################
# Version v.3.0.8 | 01-December-2021
##################################################
### Code Changes:

1. Update the insertDailySales Procedure Update
 ----------------------------------------------

CREATE DEFINER=`root`@`localhost` PROCEDURE `symplified`.`insertDailySales`()
BEGIN
   DECLARE deliveryTypes varchar(50) ;
   DECLARE i int ;
   DECLARE totalType int ;

	SELECT COUNT(*) FROM ( SELECT os.deliveryType FROM symplified.`order` os GROUP BY os.deliveryType) AS i;
	  SET i = 0;
	  WHILE i  <= 3 DO
	  	IF( i = 1 ) THEN
			INSERT INTO store_daily_sale(`date`, storeId, totalOrders, amountEarned, commision,totalServiceCharge,totalDeliveryFee, totalAmount) 
	    	SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0) + IFNULL(SUM(opd.deliveryQuotationAmount),0) , IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) , 
			IFNULL(SUM(opd.deliveryQuotationAmount),0) ,  IFNULL(SUM(o.total),0)
			FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
			WHERE 
			`created` IS NOT NULL AND o.deliveryType = "SELF"
			AND storeId IS NOT NULL 
			#AND DATE(created)=DATE(NOW()) 
			AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
			GROUP BY DATE(created), storeId
			ORDER BY DATE(created)
			ON DUPLICATE KEY UPDATE
			storeId=storeId;		
		ELSEIF( i = 2 )THEN
			INSERT INTO store_daily_sale(`date`, storeId, totalOrders, amountEarned, commision,totalServiceCharge,totalDeliveryFee, totalAmount) 
		    SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0), IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) , 
			IFNULL(SUM(opd.deliveryQuotationAmount),0) ,  IFNULL(SUM(o.total),0)
			FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
			WHERE 
			`created` IS NOT NULL AND o.deliveryType = "ADHOC"
			AND storeId IS NOT NULL 
			#AND DATE(created)=DATE(NOW()) 
			AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
			GROUP BY DATE(created), storeId
			ORDER BY DATE(created)
			ON DUPLICATE KEY UPDATE
			storeId=storeId;
		ELSE
			INSERT INTO store_daily_sale(`date`, storeId, totalOrders, amountEarned, commision,totalServiceCharge,totalDeliveryFee, totalAmount) 
		    SELECT DATE(o.created), o.storeId, COUNT(o.created), IFNULL(SUM(o.storeShare), 0), IFNULL(SUM(o.klCommission), 0),IFNULL(SUM(o.storeServiceCharges),0) , 
			IFNULL(SUM(opd.deliveryQuotationAmount),0) ,  IFNULL(SUM(o.total),0)
			FROM symplified.`order` o INNER JOIN symplified.order_payment_detail opd ON opd.orderId=o.id
			WHERE 
			`created` IS NOT NULL AND o.deliveryType = "SCHEDULED"
			AND storeId IS NOT NULL 
			#AND DATE(created)=DATE(NOW()) 
			AND  1<=(SELECT COUNT(*) FROM order_payment_status_update opsu WHERE opsu.status='PAID' AND opsu.orderId=o.id)
			GROUP BY DATE(created), storeId
			ORDER BY DATE(created)
			ON DUPLICATE KEY UPDATE
			storeId=storeId;
		END IF;
	            SET i = i + 1;

      END WHILE;
END

----------------------------------------------------------------------------------------------------------------------------------------------------------------
2. Added New point for report service 
   1. Pagination 
   2. Sort by 
   3. Limit 
   4. Swagger Endpoint Name Change






